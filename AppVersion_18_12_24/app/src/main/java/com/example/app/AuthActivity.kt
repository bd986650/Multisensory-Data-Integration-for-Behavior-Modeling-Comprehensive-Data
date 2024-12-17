@file:Suppress("DEPRECATION")

package com.example.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

@Suppress("DEPRECATION")
class   AuthActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private var selectedEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Настройка Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Обработчик кнопки "Регистрация"
        findViewById<Button>(R.id.btn_register).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Обработчик кнопки "Войти"
        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val password = findViewById<EditText>(R.id.edit_password).text.toString()
            if (selectedEmail == null) {
                Toast.makeText(this, "Пожалуйста, выберите Google аккаунт", Toast.LENGTH_SHORT).show()
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
            } else {
                sendLoginRequest(selectedEmail!!, password)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            selectedEmail = account.email
            Log.d("AuthActivity", "selected email: $selectedEmail")
            Toast.makeText(this, "Выбрана почта: $selectedEmail", Toast.LENGTH_SHORT).show()
        } catch (e: ApiException) {
            Log.w("AuthActivity", "Google Sign-In failed", e)
        }
    }

    private fun sendLoginRequest(email: String, password: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2:8080/api/auth/login"  // Эндпоинт для входа

        val requestBody = FormBody.Builder()
            .add("username", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AuthActivity, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
                }
                Log.e("AuthActivity", "Ошибка подключения: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val token = response.body?.string() // JWT токен от сервера
                    if (!token.isNullOrEmpty()) {
                        Log.d("AuthActivity", "received token: $token") // Логируем токен
                        saveToken(token)
                        runOnUiThread {
                            Toast.makeText(this@AuthActivity, "Авторизация успешна", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    // Если ошибка 401, пробуем зарегистрировать пользователя
                    if (response.code == 401) {
                        runOnUiThread {
                            Toast.makeText(this@AuthActivity, "Пользователь не найден. Выполняем регистрацию.", Toast.LENGTH_SHORT).show()
                        }
                        sendRegisterRequest(email, password)
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@AuthActivity, "Ошибка: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("AuthActivity", "Ошибка сервера: ${response.code}")
                    }
                }
            }
        })
    }

    private fun sendRegisterRequest(email: String, password: String) {
        val client = OkHttpClient()
        val url = "http://10.0.2.2:8080/api/auth/register"  // Эндпоинт для регистрации

        val requestBody = FormBody.Builder()
            .add("username", email)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AuthActivity, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show()
                }
                Log.e("AuthActivity", "Ошибка подключения: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val token = response.body?.string() // JWT токен от сервера
                    if (!token.isNullOrEmpty()) {
                        Log.d("AuthActivity", "received token: $token") // Логируем токен
                        saveToken(token)
                        runOnUiThread {
                            Toast.makeText(this@AuthActivity, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@AuthActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@AuthActivity, "Ошибка: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("AuthActivity", "Ошибка сервера: ${response.code}")
                }
            }
        })
    }


    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("jwt_token", token)
            apply()
        }
    }

    companion object {
        const val RC_SIGN_IN = 1001
    }
}
