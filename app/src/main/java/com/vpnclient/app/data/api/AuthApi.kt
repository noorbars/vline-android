package com.vpnclient.app.data.api

import com.vpnclient.app.data.api.models.CouponValidateRequest
import com.vpnclient.app.data.api.models.CouponValidateResponse
import com.vpnclient.app.data.api.models.ForgotPasswordRequest
import com.vpnclient.app.data.api.models.ForgotPasswordResponse
import com.vpnclient.app.data.api.models.LoginRequest
import com.vpnclient.app.data.api.models.OrderRenewRequest
import com.vpnclient.app.data.api.models.OrderRenewResponse
import com.vpnclient.app.data.api.models.OrderStatusResponse
import com.vpnclient.app.data.api.models.RegisterRequest
import com.vpnclient.app.data.api.models.RegisterResponse
import com.vpnclient.app.data.api.models.ResetPasswordRequest
import com.vpnclient.app.data.api.models.ResetPasswordResponse
import com.vpnclient.app.data.api.models.RenewInfoResponse
import com.vpnclient.app.data.api.models.RenewalRequest
import com.vpnclient.app.data.api.models.RenewalResponse
import com.vpnclient.app.data.api.models.WorkerLoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface AuthApi {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<WorkerLoginResponse>

    @POST("api/auth/send-register-otp")
    suspend fun sendRegisterOtp(@Body request: com.vpnclient.app.data.api.models.SendRegisterOtpRequest): Response<com.vpnclient.app.data.api.models.SendRegisterOtpResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ForgotPasswordResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ResetPasswordResponse>

    @POST("api/coupon/validate")
    suspend fun validateCoupon(@Body request: CouponValidateRequest): Response<CouponValidateResponse>

    @POST("api/renewal")
    suspend fun submitRenewal(@Body request: RenewalRequest): Response<RenewalResponse>

    @GET("api/renew-info")
    suspend fun getRenewInfo(@Query("username") username: String): Response<RenewInfoResponse>

    @POST("api/orders/renew")
    suspend fun submitOrderRenew(@Body request: OrderRenewRequest): Response<OrderRenewResponse>

    @GET("api/orders/status")
    suspend fun getOrderStatus(
        @Query("order_id") orderId: Long? = null,
        @Query("order_number") orderNumber: String? = null,
        @Query("username") username: String? = null
    ): Response<OrderStatusResponse>

    @GET
    suspend fun fetchRawSubscriptionLink(@Url subUrl: String): Response<ResponseBody>
}
