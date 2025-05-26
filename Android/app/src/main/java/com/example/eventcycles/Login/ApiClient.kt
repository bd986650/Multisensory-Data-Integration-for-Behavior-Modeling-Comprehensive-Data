package com.example.eventcycles

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private lateinit var retrofit: Retrofit
    lateinit var authService: AuthService
        private set

    fun create(context: Context, tokenManager: TokenManager) {
        val authApi = createAuthApi()

        // 1. Создаём логгер
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS // Изменено с BODY на HEADERS
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(context, tokenManager, authApi))
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl("http://51.250.108.190:8080/") // или https://
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        authService = retrofit.create(AuthService::class.java)
    }

    private fun createAuthApi(): AuthService {
        val authGson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .baseUrl("http://51.250.108.190:8080/") // тот же адрес
            .addConverterFactory(GsonConverterFactory.create(authGson))
            .build()
            .create(AuthService::class.java)
    }
}
