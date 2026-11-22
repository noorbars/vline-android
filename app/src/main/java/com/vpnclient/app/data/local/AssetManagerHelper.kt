package com.vpnclient.app.data.local

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object AssetManagerHelper {
    private const val TAG = "AssetManagerHelper"
    val GEO_FILES = listOf("geoip.dat", "geosite.dat")
    private const val MIN_VALID_SIZE = 5_000_000L // Geo files must be at least 5MB

    fun getAssetDir(context: Context): File {
        val dir = File(context.filesDir, "xray_assets")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun isGeoSiteValid(context: Context): Boolean {
        val f1 = File(getAssetDir(context), "geosite.dat")
        val f2 = File(context.filesDir, "geosite.dat")
        return (f1.exists() && f1.length() >= MIN_VALID_SIZE) || (f2.exists() && f2.length() >= MIN_VALID_SIZE)
    }

    fun isGeoIpValid(context: Context): Boolean {
        val f1 = File(getAssetDir(context), "geoip.dat")
        val f2 = File(context.filesDir, "geoip.dat")
        return (f1.exists() && f1.length() >= MIN_VALID_SIZE) || (f2.exists() && f2.length() >= MIN_VALID_SIZE)
    }

    fun copyGeoAssetsIfNeeded(context: Context) {
        val targetDirs = listOf(getAssetDir(context), context.filesDir)

        for (dir in targetDirs) {
            for (fileName in GEO_FILES) {
                val targetFile = File(dir, fileName)
                // If corrupted or partial (less than 5MB), delete it to prevent EOF crashes
                if (targetFile.exists() && targetFile.length() < MIN_VALID_SIZE) {
                    Log.w(TAG, "Deleting corrupt/truncated $fileName (${targetFile.length()} bytes)")
                    targetFile.delete()
                }

                if (!targetFile.exists()) {
                    try {
                        context.assets.open(fileName).use { input ->
                            val tempFile = File(dir, "$fileName.tmp")
                            FileOutputStream(tempFile).use { output ->
                                val buffer = ByteArray(64 * 1024)
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                }
                                output.flush()
                            }

                            if (tempFile.length() >= MIN_VALID_SIZE) {
                                if (targetFile.exists()) targetFile.delete()
                                tempFile.renameTo(targetFile)
                                Log.i(TAG, "Successfully extracted $fileName (${targetFile.length()} bytes) to ${targetFile.absolutePath}")
                            } else {
                                Log.w(TAG, "Extraction of $fileName produced partial file (${tempFile.length()} bytes), discarding")
                                tempFile.delete()
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Could not extract asset $fileName: ${e.message}")
                    }
                }
            }
        }
    }
}
