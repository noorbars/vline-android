package com.vpnclient.app.data.api.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: WorkerSubscriptionData? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)

data class WorkerLoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: WorkerSubscriptionData?,
    @SerializedName("error") val error: String?
)

data class WorkerSubscriptionData(
    @SerializedName("sub_url") val subUrl: String?,
    @SerializedName("expire_date") val expireDate: String?,
    @SerializedName("title") val title: String?
)

data class RenewalRequest(
    @SerializedName("username") val username: String,
    @SerializedName("plan_id") val planId: String,
    @SerializedName("plan_title") val planTitle: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("tracking_code") val trackingCode: String,
    @SerializedName("receipt_base64") val receiptBase64: String? = null,
    @SerializedName("note") val note: String = ""
)

data class RenewalResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("order_number") val orderNumber: String? = null,
    @SerializedName("order_id") val orderId: Long? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)

// --- Full-Stack Renewal Architecture Models ---

data class RenewInfoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: RenewInfoData?,
    @SerializedName("error") val error: String?
)

data class RenewInfoData(
    @SerializedName("plans") val plans: List<RenewalPlanDto>,
    @SerializedName("card_info") val cardInfo: CardInfoDto,
    @SerializedName("wallet_balance") val walletBalance: Long,
    @SerializedName("subscription") val subscription: SubscriptionStatusDto,
    @SerializedName("pending_order") val pendingOrder: PendingOrderDto?
)

data class RenewalPlanDto(
    @SerializedName("id") val id: String,
    @SerializedName("plan_key") val planKey: String,
    @SerializedName("title") val title: String,
    @SerializedName("traffic") val traffic: String,
    @SerializedName("traffic_gb") val trafficGb: Int,
    @SerializedName("duration_days") val durationDays: Int,
    @SerializedName("price") val price: Long,
    @SerializedName("tag") val tag: String?
)

data class CardInfoDto(
    @SerializedName("card_number") val cardNumber: String,
    @SerializedName("card_name") val cardName: String
)

data class SubscriptionStatusDto(
    @SerializedName("username") val username: String,
    @SerializedName("title") val title: String?,
    @SerializedName("expire_date") val expireDate: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("is_expired") val isExpired: Boolean,
    @SerializedName("telegram_id") val telegramId: String?
)

data class PendingOrderDto(
    @SerializedName("order_id") val orderId: Long,
    @SerializedName("order_number") val orderNumber: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String?
)

data class OrderRenewRequest(
    @SerializedName("username") val username: String,
    @SerializedName("plan_id") val planId: String,
    @SerializedName("payment_method") val paymentMethod: String, // "wallet" or "card"
    @SerializedName("tracking_code") val trackingCode: String? = null,
    @SerializedName("receipt_base64") val receiptBase64: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("coupon_code") val couponCode: String? = null
)

data class CouponValidateRequest(
    @SerializedName("code") val code: String,
    @SerializedName("plan_id") val planId: String,
    @SerializedName("username") val username: String? = null
)

data class CouponValidateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: CouponData? = null,
    @SerializedName("error") val error: String? = null
)

data class CouponData(
    @SerializedName("code") val code: String,
    @SerializedName("discount_amount") val discountAmount: Long,
    @SerializedName("final_price") val finalPrice: Long,
    @SerializedName("message") val message: String? = null
)

data class OrderRenewResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("is_approved") val isApproved: Boolean?,
    @SerializedName("order_id") val orderId: Long?,
    @SerializedName("order_number") val orderNumber: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("new_expire_date") val newExpireDate: String?,
    @SerializedName("wallet_balance_after") val walletBalanceAfter: Long?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)

data class OrderStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: OrderStatusData?,
    @SerializedName("error") val error: String?
)

data class OrderStatusData(
    @SerializedName("order_id") val orderId: Long,
    @SerializedName("order_number") val orderNumber: String,
    @SerializedName("status") val status: String,
    @SerializedName("is_approved") val isApproved: Boolean,
    @SerializedName("is_pending") val isPending: Boolean,
    @SerializedName("is_cancelled") val isCancelled: Boolean,
    @SerializedName("amount") val amount: Long,
    @SerializedName("plan_name") val planName: String?,
    @SerializedName("expire_date") val expireDate: String?,
    @SerializedName("is_sub_active") val isSubActive: Boolean,
    @SerializedName("message") val message: String?
)
