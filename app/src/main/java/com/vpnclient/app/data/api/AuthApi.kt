package com.vpnclient.app.data.api

import com.vpnclient.app.data.api.models.LoginRequest
import com.vpnclient.app.data.api.models.RenewalRequest
import com.vpnclient.app.data.api.models.RenewalResponse
import com.vpnclient.app.data.api.models.WorkerLoginResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthApi {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<WorkerLoginResponse>

    @POST("api/renewal")
    suspend fun submitRenewal(@Body request: RenewalRequest): Response<RenewalResponse>

    @GET
    suspend fun fetchRawSubscriptionLink(@Url subUrl: String): Response<ResponseBody>
}
