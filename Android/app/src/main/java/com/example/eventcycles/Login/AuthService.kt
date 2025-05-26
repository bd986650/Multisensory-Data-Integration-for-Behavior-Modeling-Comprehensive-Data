package com.example.eventcycles

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @POST("api/auth/register")
    fun register(
        @Query("username") username: String,
        @Query("password") password: String
    ): Call<ResponseBody>

    @POST("api/auth/login")
    fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): Call<ResponseBody>

    @POST("api/auth/refresh")
    fun refresh(@Header("Authorization") refreshToken: String): Call<ResponseBody>

    @POST("/api/users/save")
    fun sendMetrics(
        @Body metricRequest: MetricRequestDto
    ): Call<ResponseBody>

    @POST("/api/users/save")
    fun sendMetricsStr(
        @Body metricRequestStr: MetricRequestDtoStr
    ): Call<ResponseBody>
}
