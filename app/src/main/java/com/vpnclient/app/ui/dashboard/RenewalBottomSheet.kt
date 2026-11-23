package com.vpnclient.app.ui.dashboard

// Re-exports from RenewScreen.kt for seamless compatibility
data class RenewalPlan(
    val id: String,
    val title: String,
    val traffic: String,
    val priceToman: Long,
    val tag: String? = null
)

val RENEWAL_PLANS = listOf(
    RenewalPlan("smart_1m", "۱ ماهه (۳۰ گیگ)", "۳۰ گیگابایت", 299000L),
    RenewalPlan("smart_3m", "۳ ماهه (۹۰ گیگ)", "۹۰ گیگابایت", 819000L, "پیشنهادی ⭐"),
    RenewalPlan("smart_6m", "۶ ماهه (۱۲۰ گیگ)", "۱۲۰ گیگابایت", 1519000L),
    RenewalPlan("smart_12m", "۱۲ ماهه (۲۴۰ گیگ)", "۲۴۰ گیگابایت", 2899000L, "اقتصادی")
)
