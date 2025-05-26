package com.example.eventcycles

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

class AuthInterceptor(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val apiService: AuthService
) : Interceptor {

    @Synchronized
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val isRefreshRequest = request.url.encodedPath.contains("/refresh")

        val accessToken = tokenManager.getAccessToken(context)
        if (!isRefreshRequest && !accessToken.isNullOrEmpty()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        }

        var response = chain.proceed(request)

        if (response.code == 401) {
            response.close() // обязательно закрываем старый ответ

            val refreshToken = tokenManager.getRefreshToken(context)
            if (!refreshToken.isNullOrEmpty()) {
                val refreshResponse = apiService.refresh("Bearer $refreshToken").execute()

                if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                    val responseBodyStr = refreshResponse.body()!!.string()

                    try {
                        val json = JSONObject(responseBodyStr)
                        val newAccessToken = json.getString("accessToken")
                        val newRefreshToken = json.getString("refreshToken")

                        val newTokens = AuthResponse(newAccessToken, newRefreshToken)
                        tokenManager.saveTokens(context, newTokens)

                        val newRequest = request.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $newAccessToken")
                            .build()

                        return chain.proceed(newRequest)
                    } catch (e: Exception) {
                        // Ошибка парсинга токенов — чистим токены
                        tokenManager.clearTokens(context)
                    }
                } else {
                    tokenManager.clearTokens(context)
                }
            }
        }

        return response
    }
}
