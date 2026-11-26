package com.vpnclient.app.data.api.models

import com.google.gson.annotations.SerializedName
import java.util.Locale
import java.util.concurrent.TimeUnit

data class UserProfile(
    @SerializedName("username") val username: String,
    @SerializedName("trafficLimitBytes") val trafficLimitBytes: Long = 0L,
    @SerializedName("trafficUsedBytes") val trafficUsedBytes: Long = 0L,
    @SerializedName("expireTimestamp") val expireTimestamp: Long = 0L // Milliseconds or Unix seconds
) {
    val remainingBytes: Long
        get() {
            if (trafficLimitBytes <= 0L) return -1L // Unlimited
            val remaining = trafficLimitBytes - trafficUsedBytes
            return if (remaining > 0L) remaining else 0L
        }

    val remainingDays: Long
        get() {
            if (expireTimestamp <= 0L) return -1L // Unlimited
            // Normalize seconds to milliseconds if needed
            val expireMillis = if (expireTimestamp < 10000000000L) expireTimestamp * 1000 else expireTimestamp
            val diff = expireMillis - System.currentTimeMillis()
            return if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff) else 0L
        }

    val isExpired: Boolean
        get() {
            if (expireTimestamp <= 0L) return false
            val expireMillis = if (expireTimestamp < 10000000000L) expireTimestamp * 1000 else expireTimestamp
            return expireMillis <= System.currentTimeMillis()
        }

    val daysRemaining: Long
        get() = remainingDays

    val trafficPercentRemaining: Int
        get() = if (trafficLimitBytes > 0L) {
            val rem = remainingBytes
            if (rem > 0L) {
                ((rem.toDouble() / trafficLimitBytes) * 100).toInt().coerceIn(0, 100)
            } else 0
        } else {
            100
        }

    val daysPercentRemaining: Int
        get() {
            if (remainingDays <= 0L) return 0
            val base = when {
                remainingDays > 180 -> 365
                remainingDays > 90 -> 180
                remainingDays > 30 -> 90
                else -> 30
            }
            return ((remainingDays.toDouble() / base) * 100).toInt().coerceIn(0, 100)
        }

    fun formattedRemainingTraffic(): String {
        val bytes = remainingBytes
        if (bytes < 0L) return "نامحدود"
        val gb = bytes.toDouble() / (1024 * 1024 * 1024)
        return if (gb >= 1.0) {
            val rounded = (gb * 10).toLong() / 10.0
            if (rounded == rounded.toLong().toDouble() || String.format(Locale.US, "%.1f", gb).endsWith(".0")) {
                "${rounded.toLong()} گیگابایت"
            } else {
                String.format(Locale.US, "%.1f گیگابایت", gb)
            }
        } else {
            val mb = bytes.toDouble() / (1024 * 1024)
            "${mb.toLong()} مگابایت"
        }
    }
}
