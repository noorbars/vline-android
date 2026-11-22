package com.vpnclient.app.data.api

import com.vpnclient.app.data.local.PreferenceManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Configurable backend API base URL (can be changed to your domain/VPS IP)
    var BASE_URL = "https://vline-vip-bot.gibfo5302.workers.dev/"

    private var retrofit: Retrofit? = null

    fun getService(preferenceManager: PreferenceManager): AuthApi {
        if (retrofit == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(preferenceManager))
                .addInterceptor(loggingInterceptor)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(AuthApi::class.java)
    }

    /**
     * Updates the base URL dynamically if required (e.g. from user settings or fallback mirror)
     */
    fun updateBaseUrl(newBaseUrl: String, preferenceManager: PreferenceManager) {
        BASE_URL = if (newBaseUrl.endsWith("/")) newBaseUrl else "$newBaseUrl/"
        retrofit = null // Rebuild on next request
    }
}
