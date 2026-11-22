package com.vpnclient.app.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.gson.JsonParser
import com.vpnclient.app.data.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class UpdateManager(
    private val context: Context,
    private val gitHubRepo: String = "noorbars/vline-android"
) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Checks GitHub Releases API for the latest version of the app.
     */
    suspend fun checkForUpdate(currentVersionName: String): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$gitHubRepo/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "VLine-Android-App")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 404) {
                        return@withContext Result.success(
                            AppUpdateInfo(
                                hasUpdate = false,
                                latestVersion = currentVersionName,
                                currentVersion = currentVersionName,
                                releaseTitle = "آخرین نسخه",
                                releaseNotes = "هنوز نسخه‌ای در مخزن گیت‌هاب منتشر نشده است.",
                                downloadUrl = null,
                                htmlUrl = "https://github.com/$gitHubRepo"
                            )
                        )
                    }
                    return@withContext Result.failure(Exception("خطا در ارتباط با سرور گیت‌هاب (کد ${response.code})"))
                }

                val bodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("پاسخ سرور خالی است"))
                val json = JsonParser.parseString(bodyStr).asJsonObject

                val tagName = json.get("tag_name")?.asString?.removePrefix("v")?.trim() ?: currentVersionName
                val releaseTitle = json.get("name")?.asString ?: "نسخه $tagName"
                val releaseNotes = json.get("body")?.asString ?: "تغییرات این نسخه ثبت نشده است."
                val htmlUrl = json.get("html_url")?.asString ?: "https://github.com/$gitHubRepo"

                // Find APK asset in the release
                var downloadUrl: String? = null
                var fileSize = 0L

                val assets = json.getAsJsonArray("assets")
                if (assets != null) {
                    for (assetElement in assets) {
                        val assetObj = assetElement.asJsonObject
                        val name = assetObj.get("name")?.asString ?: ""
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = assetObj.get("browser_download_url")?.asString
                            fileSize = assetObj.get("size")?.asLong ?: 0L
                            break
                        }
                    }
                }

                val hasUpdate = isNewerVersion(tagName, currentVersionName) && downloadUrl != null

                Result.success(
                    AppUpdateInfo(
                        hasUpdate = hasUpdate,
                        latestVersion = tagName,
                        currentVersion = currentVersionName,
                        releaseTitle = releaseTitle,
                        releaseNotes = releaseNotes,
                        downloadUrl = downloadUrl,
                        fileSize = fileSize,
                        htmlUrl = htmlUrl
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Downloads the APK file to app cache with real-time progress callbacks.
     */
    suspend fun downloadApk(
        downloadUrl: String,
        targetFileName: String,
        onProgress: (percent: Int, bytesRead: Long, totalBytes: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "VLine-Android-App")
                .build()

            val updateDir = File(context.cacheDir, "updates").apply { if (!exists()) mkdirs() }
            val outputFile = File(updateDir, targetFileName)

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("خطا در دانلود فایل (کد ${response.code})"))
                }

                val body = response.body ?: return@withContext Result.failure(Exception("فایل دریافتی خالی است"))
                val totalBytes = body.contentLength()

                body.byteStream().use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Long = 0
                        var read: Int

                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            bytesRead += read
                            val percent = if (totalBytes > 0) ((bytesRead * 100) / totalBytes).toInt() else -1
                            onProgress(percent, bytesRead, totalBytes)
                        }
                        output.flush()
                    }
                }
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Launches Android Package Installer to install the downloaded APK file.
     */
    fun installApk(apkFile: File) {
        if (!apkFile.exists()) return

        // For Android 8.0+ check unknown sources permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(settingsIntent)
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(installIntent)
    }

    /**
     * SemVer comparator: returns true if remote version > current version.
     */
    fun isNewerVersion(remote: String, current: String): Boolean {
        try {
            val remoteParts = remote.split(".").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }
}
