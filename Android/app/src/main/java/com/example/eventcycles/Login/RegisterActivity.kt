package com.example.eventcycles

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPasswordConfirm: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ApiClient.create(this, TokenManager)

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etPasswordConfirm = findViewById(R.id.et_password_confirm)
        btnRegister = findViewById(R.id.btn_register)
        tvGoToLogin = findViewById(R.id.tv_go_to_login)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirm = etPasswordConfirm.text.toString()

            if (password != confirm) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password)
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(username: String, password: String) {
        ApiClient.authService.register(username, password).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(this@RegisterActivity, "Успешная регистрация", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    val errorText = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        "Не удалось прочитать ошибку"
                    }

                    Log.e("RegisterError", "Ошибка регистрации: $errorText")
                    Toast.makeText(this@RegisterActivity, "Ошибка регистрации: $errorText", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
