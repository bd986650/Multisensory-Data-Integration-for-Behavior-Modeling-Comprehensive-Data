package com.example.eventcycles

import android.content.Context
import org.json.JSONObject
import android.util.Base64
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val ACCESS_TOKEN = "access_token"
    private const val REFRESH_TOKEN = "refresh_token"

    private val tokenMutex = Mutex()

    suspend fun <T> withTokenLock(block: suspend () -> T): T {
        return tokenMutex.withLock {
            block()
        }
    }

    fun saveTokens(context: Context, response: AuthResponse) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(ACCESS_TOKEN, response.accessToken)
            putString(REFRESH_TOKEN, response.refreshToken)
            apply()
        }
    }

    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(REFRESH_TOKEN, null)
    }

    fun clearTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }


    fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
            val json = JSONObject(payload)
            val exp = json.getLong("exp")
            val now = System.currentTimeMillis() / 1000

            exp < now
        } catch (e: Exception) {
            true // Если ошибка — считаем токен просроченным
        }
    }
}
