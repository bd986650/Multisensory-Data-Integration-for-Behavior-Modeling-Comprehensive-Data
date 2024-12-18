package com.example.app

import android.app.Activity
import android.content.Context
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AuthManager(private val context: Context) {

    fun sendDataToServer(jsonData: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2:8080/api/users/save-data"

        val token = getAuthToken()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .post(jsonData.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, "Ошибка отправки данных на сервер", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    (context as? Activity)?.runOnUiThread {
                        Toast.makeText(context, "Данные успешно отправлены на сервер", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getAuthToken(): String {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", "") ?: ""
    }
}