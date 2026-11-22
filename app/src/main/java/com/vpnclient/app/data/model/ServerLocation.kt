package com.vpnclient.app.data.model

data class ServerLocation(
    val id: String,
    val countryName: String,
    val flagEmoji: String,
    val isAvailable: Boolean = true,
    val latencyLabel: String? = null
)

object LocationManager {
    val availableLocations = listOf(
        ServerLocation(
            id = "uk",
            countryName = "انگلستان",
            flagEmoji = "🇬🇧",
            isAvailable = true,
            latencyLabel = "کمترین تاخیر (پیشنهادی)"
        ),
        ServerLocation(
            id = "de",
            countryName = "آلمان",
            flagEmoji = "🇩🇪",
            isAvailable = false,
            latencyLabel = "بزودی"
        ),
        ServerLocation(
            id = "nl",
            countryName = "هلند",
            flagEmoji = "🇳🇱",
            isAvailable = false,
            latencyLabel = "بزودی"
        ),
        ServerLocation(
            id = "fr",
            countryName = "فرانسه",
            flagEmoji = "🇫🇷",
            isAvailable = false,
            latencyLabel = "بزودی"
        )
    )

    fun getActiveLocation(id: String): ServerLocation {
        return availableLocations.firstOrNull { it.id == id && it.isAvailable } ?: availableLocations.first()
    }
}
