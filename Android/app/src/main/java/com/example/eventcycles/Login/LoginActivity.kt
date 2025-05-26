package com.example.eventcycles

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализируем ApiClient перед использованием
        ApiClient.create(this, TokenManager)

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvGoToRegister = findViewById(R.id.tv_go_to_register)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            loginUser(email, password)
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        // Валидация полей
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.authService.login(username, password).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    if (response.isSuccessful && response.body() != null) {
                        // Преобразуем тело ответа в строку
                        val responseBody = response.body()!!.string()
                        Log.d("LoginDebug", "Login response body: $responseBody")

                        val json = JSONObject(responseBody)

                        val accessToken = json.getString("accessToken")
                        val refreshToken = json.getString("refreshToken")

                        val authResponse = AuthResponse(accessToken, refreshToken)
                        TokenManager.saveTokens(this@LoginActivity, authResponse)

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // Улучшенная обработка ошибок от сервера
                        val errorBody = response.errorBody()?.string() ?: "Неизвестная ошибка"
                        val errorMessage = when (response.code()) {
                            401 -> "Неверный логин или пароль"
                            400 -> "Неверный формат данных"
                            500 -> "Ошибка сервера"
                            else -> "Ошибка входа: $errorBody"
                        }
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("LoginError", "Code: ${response.code()}, Body: $errorBody")
                    }
                } catch (e: Exception) {
                    // Обработка всех ошибок
                    Toast.makeText(this@LoginActivity, "Ошибка при обработке ответа: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginError", "Error processing response: ${e.message}", e)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Улучшенные сообщения об ошибках сети
                val errorMessage = when (t) {
                    is java.net.UnknownHostException -> "Нет подключения к интернету"
                    is java.net.SocketTimeoutException -> "Превышено время ожидания"
                    else -> "Ошибка сети: ${t.message}"
                }
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("LoginError", "Network error: ${t.message}", t)
            }
        })
    }
}
