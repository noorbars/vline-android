package com.vpnclient.app.data.repository

import com.google.gson.Gson
import com.vpnclient.app.data.api.ApiClient
import com.vpnclient.app.data.api.models.LoginRequest
import com.vpnclient.app.data.api.models.UserProfile
import com.vpnclient.app.data.api.models.WorkerLoginResponse
import com.vpnclient.app.data.local.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AuthRepository(private val preferenceManager: PreferenceManager) {

    private val gson = Gson()

    suspend fun login(username: String, password: String): Result<WorkerLoginResponse> =
        withContext(Dispatchers.IO) {
            try {
                val api = ApiClient.getService(preferenceManager)
                val response = api.login(LoginRequest(username.trim(), password.trim()))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.success && body.data != null) {
                        val data = body.data
                        preferenceManager.username = username.trim()
                        preferenceManager.password = password.trim()
                        preferenceManager.subUrl = data.subUrl

                        // Parse expire_date from Cloudflare D1
                        val expireTimestamp = parseExpireDate(data.expireDate)
                        val profile = UserProfile(
                            username = username.trim(),
                            expireTimestamp = expireTimestamp
                        )
                        preferenceManager.cachedUserProfile = profile

                        Result.success(body)
                    } else {
                        val errMsg = body.error ?: "نام کاربری یا رمز عبور نامعتبر است"
                        Result.failure(Exception(errMsg))
                    }
                } else {
                    // Extract error from response body if possible
                    val errBody = response.errorBody()?.string()
                    val parsedErr = try {
                        val errObj = gson.fromJson(errBody, WorkerLoginResponse::class.java)
                        errObj?.error
                    } catch (e: Exception) {
                        null
                    }
                    val msg = parsedErr ?: "نام کاربری یا رمز عبور اشتباه است (کد ${response.code()})"
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun logout() {
        preferenceManager.clearAuth()
    }

    fun isLoggedIn(): Boolean = preferenceManager.isLoggedIn

    fun getCachedUsername(): String = preferenceManager.username ?: ""

    fun getCachedPassword(): String = preferenceManager.password ?: ""

    private fun parseExpireDate(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return 0L
        val formats = listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd"
        )
        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = sdf.parse(dateStr)
                if (date != null) return date.time
            } catch (ignored: Exception) {
            }
        }
        return 0L
    }
}
