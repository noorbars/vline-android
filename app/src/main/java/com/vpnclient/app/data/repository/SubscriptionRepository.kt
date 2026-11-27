package com.vpnclient.app.data.repository

import android.util.Base64
import com.vpnclient.app.data.api.ApiClient
import com.vpnclient.app.data.api.models.UserProfile
import com.vpnclient.app.data.local.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionRepository(
    private val preferenceManager: PreferenceManager,
    private val authRepository: AuthRepository
) {

    suspend fun refreshSubscription(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val username = preferenceManager.username
            val password = preferenceManager.password

            // If user has saved credentials, re-validate with Worker D1 to ensure subscription is active
            if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                val loginResult = authRepository.login(username, password)
                if (loginResult.isFailure) {
                    val local = preferenceManager.cachedSubscriptionConfig
                    return@withContext if (!local.isNullOrBlank()) {
                        Result.success(local)
                    } else {
                        Result.failure(loginResult.exceptionOrNull() ?: Exception("احراز هویت اشتراک ناموفق بود"))
                    }
                }
            }

            val subUrl = preferenceManager.subUrl
            if (!subUrl.isNullOrBlank()) {
                fetchFromSubUrl(subUrl)
            } else {
                val local = preferenceManager.cachedSubscriptionConfig
                if (!local.isNullOrBlank()) {
                    Result.success(local)
                } else {
                    Result.failure(Exception("لینک اشتراک یافت نشد"))
                }
            }
        } catch (e: Exception) {
            val local = preferenceManager.cachedSubscriptionConfig
            if (!local.isNullOrBlank()) {
                Result.success(local)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun fetchFromSubUrl(subUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val api = ApiClient.getService(preferenceManager)
            val response = api.fetchRawSubscriptionLink(subUrl)

            if (response.isSuccessful && response.body() != null) {
                // 1. Parse 3X-UI Subscription-Userinfo header if available:
                // Format: upload=1234; download=5678; total=53687091200; expire=1732500000
                val userInfoHeader = response.headers()["Subscription-Userinfo"]
                if (!userInfoHeader.isNullOrBlank()) {
                    updateUserProfileFromHeader(userInfoHeader)
                }

                // 2. Decode raw base64 VLESS configs
                val rawBody = response.body()!!.string().trim()
                val decoded = decodeSubscriptionContent(rawBody)
                preferenceManager.cachedSubscriptionConfig = decoded
                Result.success(decoded)
            } else {
                Result.failure(Exception("خطا در دریافت اشتراک از سرور (کد ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun updateUserProfileFromHeader(header: String) {
        try {
            var upload = 0L
            var download = 0L
            var total = 0L
            var expire = 0L

            val parts = header.split(";")
            for (part in parts) {
                val kv = part.trim().split("=")
                if (kv.size == 2) {
                    val key = kv[0].trim().lowercase()
                    val value = kv[1].trim().toLongOrNull() ?: 0L
                    when (key) {
                        "upload" -> upload = value
                        "download" -> download = value
                        "total" -> total = value
                        "expire" -> expire = value
                    }
                }
            }

            val currentProfile = preferenceManager.cachedUserProfile
            val username = preferenceManager.username ?: ""
            val expireTimestamp = if (expire > 0) {
                if (expire < 10000000000L) expire * 1000 else expire
            } else {
                currentProfile?.expireTimestamp ?: 0L
            }

            val updated = UserProfile(
                username = username,
                trafficLimitBytes = total,
                trafficUsedBytes = upload + download,
                expireTimestamp = expireTimestamp
            )
            preferenceManager.cachedUserProfile = updated
        } catch (ignored: Exception) {
        }
    }

    fun getCachedConfig(): String? = preferenceManager.cachedSubscriptionConfig

    fun getCachedProfile(): UserProfile? = preferenceManager.cachedUserProfile

    suspend fun submitRenewal(
        planId: String,
        planTitle: String,
        amount: Long,
        trackingCode: String,
        receiptBase64: String?,
        note: String
    ): Result<com.vpnclient.app.data.api.models.RenewalResponse> = withContext(Dispatchers.IO) {
        try {
            val username = preferenceManager.username ?: ""
            val api = ApiClient.getService(preferenceManager)
            val request = com.vpnclient.app.data.api.models.RenewalRequest(
                username = username,
                planId = planId,
                planTitle = planTitle,
                amount = amount,
                trackingCode = trackingCode,
                receiptBase64 = receiptBase64,
                note = note
            )
            val response = api.submitRenewal(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.error ?: "خطا در ثبت تمدید"))
                }
            } else {
                Result.failure(Exception("خطا در ارسال درخواست به سرور (کد ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRenewInfo(usernameOverride: String? = null): Result<com.vpnclient.app.data.api.models.RenewInfoData> = withContext(Dispatchers.IO) {
        try {
            val username = usernameOverride ?: preferenceManager.username ?: ""
            val api = ApiClient.getService(preferenceManager)
            val response = api.getRenewInfo(username)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    val serverEmail = body.data.subscription.email?.takeIf { it.isNotBlank() }
                    val serverUser = serverEmail ?: body.data.subscription.username.takeIf { it.isNotBlank() }
                    if (!serverUser.isNullOrBlank()) {
                        preferenceManager.username = serverUser
                    }
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.error ?: "اطلاعات تمدید یافت نشد"))
                }
            } else {
                Result.failure(Exception("خطا در دریافت اطلاعات تمدید (کد ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitOrderRenew(
        planId: String,
        paymentMethod: String, // "wallet" or "card"
        trackingCode: String? = null,
        receiptBase64: String? = null,
        note: String? = null,
        couponCode: String? = null,
        useWallet: Boolean = false
    ): Result<com.vpnclient.app.data.api.models.OrderRenewResponse> = withContext(Dispatchers.IO) {
        try {
            val username = preferenceManager.username ?: ""
            val api = ApiClient.getService(preferenceManager)
            val request = com.vpnclient.app.data.api.models.OrderRenewRequest(
                username = username,
                planId = planId,
                paymentMethod = paymentMethod,
                trackingCode = trackingCode,
                receiptBase64 = receiptBase64,
                note = note,
                couponCode = couponCode,
                useWallet = useWallet
            )
            val response = api.submitOrderRenew(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body.error ?: "خطا در ثبت سفارش تمدید"))
                }
            } else {
                Result.failure(Exception("خطا در ارسال سفارش به سرور (کد ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateCoupon(
        code: String,
        planId: String
    ): Result<com.vpnclient.app.data.api.models.CouponData> = withContext(Dispatchers.IO) {
        try {
            val username = preferenceManager.username
            val api = ApiClient.getService(preferenceManager)
            val response = api.validateCoupon(
                com.vpnclient.app.data.api.models.CouponValidateRequest(
                    code = code.trim(),
                    planId = planId,
                    username = username
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.error ?: "کد تخفیف نامعتبر است"))
                }
            } else {
                val errBody = response.errorBody()?.string()
                val parsedErr = try {
                    com.google.gson.Gson().fromJson(
                        errBody,
                        com.vpnclient.app.data.api.models.CouponValidateResponse::class.java
                    )?.error
                } catch (e: Exception) {
                    null
                }
                Result.failure(Exception(parsedErr ?: "کد تخفیف نامعتبر یا منقضی است (کد ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrderStatus(
        orderId: Long? = null,
        orderNumber: String? = null,
        usernameOverride: String? = null
    ): Result<com.vpnclient.app.data.api.models.OrderStatusData> = withContext(Dispatchers.IO) {
        try {
            val username = usernameOverride ?: preferenceManager.username
            val api = ApiClient.getService(preferenceManager)
            val response = api.getOrderStatus(orderId = orderId, orderNumber = orderNumber, username = username)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body.error ?: "سفارش یافت نشد"))
                }
            } else {
                Result.failure(Exception("خطا در استعلام وضعیت سفارش (کد ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decodeSubscriptionContent(content: String): String {
        return try {
            val clean = content.replace("\n", "").replace("\r", "").trim()
            val decodedBytes = Base64.decode(clean, Base64.DEFAULT)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            if (decodedString.contains("://")) {
                decodedString
            } else {
                content
            }
        } catch (e: Exception) {
            content
        }
    }
}
