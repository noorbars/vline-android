package com.vpnclient.app.data.api.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
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
