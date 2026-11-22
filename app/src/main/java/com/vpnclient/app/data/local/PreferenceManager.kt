package com.vpnclient.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.vpnclient.app.data.api.models.UserProfile

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for devices with hardware keystore issues
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val gson = Gson()

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    var password: String?
        get() = prefs.getString(KEY_PASSWORD, null)
        set(value) = prefs.edit().putString(KEY_PASSWORD, value).apply()

    var subUrl: String?
        get() = prefs.getString(KEY_SUB_URL, null)
        set(value) = prefs.edit().putString(KEY_SUB_URL, value).apply()

    var cachedSubscriptionConfig: String?
        get() = prefs.getString(KEY_CACHED_CONFIG, null)
        set(value) = prefs.edit().putString(KEY_CACHED_CONFIG, value).apply()

    var cachedUserProfile: UserProfile?
        get() {
            val json = prefs.getString(KEY_USER_PROFILE, null) ?: return null
            return try {
                gson.fromJson(json, UserProfile::class.java)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            val json = if (value != null) gson.toJson(value) else null
            prefs.edit().putString(KEY_USER_PROFILE, json).apply()
        }

    var isIranBypassEnabled: Boolean
        get() = prefs.getBoolean(KEY_IRAN_BYPASS, true) // Enabled by default
        set(value) = prefs.edit().putBoolean(KEY_IRAN_BYPASS, value).apply()

    var selectedRouteCategory: String
        get() = prefs.getString(KEY_ROUTE_CATEGORY, "smart") ?: "smart"
        set(value) = prefs.edit().putString(KEY_ROUTE_CATEGORY, value).apply()

    var selectedLocationId: String
        get() = prefs.getString(KEY_LOCATION_ID, "uk") ?: "uk"
        set(value) = prefs.edit().putString(KEY_LOCATION_ID, value).apply()

    fun clearAuth() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USERNAME)
            .remove(KEY_PASSWORD)
            .remove(KEY_SUB_URL)
            .remove(KEY_CACHED_CONFIG)
            .remove(KEY_USER_PROFILE)
            .apply()
    }

    val isLoggedIn: Boolean
        get() = !username.isNullOrBlank() && !password.isNullOrBlank()

    companion object {
        private const val PREFS_NAME = "vpn_secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_SUB_URL = "sub_url"
        private const val KEY_CACHED_CONFIG = "cached_subscription_config"
        private const val KEY_USER_PROFILE = "cached_user_profile"
        private const val KEY_IRAN_BYPASS = "iran_bypass_enabled"
        private const val KEY_ROUTE_CATEGORY = "selected_route_category"
        private const val KEY_LOCATION_ID = "selected_location_id"
    }
}
