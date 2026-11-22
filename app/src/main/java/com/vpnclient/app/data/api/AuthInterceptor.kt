package com.vpnclient.app.data.api

import com.vpnclient.app.data.local.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val preferenceManager: PreferenceManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = preferenceManager.authToken

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank() && originalRequest.header("Authorization") == null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        requestBuilder.addHeader("User-Agent", "VLineVpnClient/1.0 (Android)")
        return chain.proceed(requestBuilder.build())
    }
}
