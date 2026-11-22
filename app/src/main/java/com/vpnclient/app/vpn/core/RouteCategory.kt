package com.vpnclient.app.vpn.core

/**
 * Route choices with clear Persian descriptions and technical details.
 * Inbound numbers are deliberately omitted per user design guidelines.
 */
enum class RouteCategory(
    val key: String,
    val title: String,
    val description: String,
    val technicalTag: String,
    val isRecommended: Boolean = false
) {
    SMART(
        key = "smart",
        title = "اتصال هوشمند (پیشنهادی)",
        description = "تست خودکار و اتصال به سریع‌ترین و پایدارترین مسیر با کمترین تاخیر",
        technicalTag = "هوشمند • پایش خودکار TLS / Reality / IPv6",
        isRecommended = true
    ),
    STABLE(
        key = "stable",
        title = "مسیر اصلی (پایدار)",
        description = "پروتکل VLESS + TLS با بالاترین ماندگاری و پایداری اتصال",
        technicalTag = "پروتکل TLS • اتصال پایدار"
    ),
    FAST(
        key = "fast",
        title = "مسیر پرسرعت",
        description = "پروتکل VLESS + Reality با الگوریتم ضد اختلال و سرعت دانلود بالا",
        technicalTag = "پروتکل Reality • سرعت بالا"
    ),
    SPECIAL_IR(
        key = "special_ir",
        title = "مسیر ویژه همراه اول و ایرانسل",
        description = "مسیریابی بر بستر شبکه IPv6 بهینه‌سازی شده برای دیتای سیم‌کارت",
        technicalTag = "پروتکل IPv6 • ویژه دکل‌های همراه"
    );

    companion object {
        fun fromKey(key: String?): RouteCategory {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SMART
        }
    }
}
