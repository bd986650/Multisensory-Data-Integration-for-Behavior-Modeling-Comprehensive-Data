package com.example.eventcycles

import android.content.Context
import android.util.Log
import okhttp3.ResponseBody
import org.osmdroid.library.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ProtocolException
import kotlinx.coroutines.*

object MetricSender {

    fun sendSteps(context: Context, steps: Int, timestamp: Long) {
        val token = TokenManager.getAccessToken(context)
        Log.d("MetricSender", "Access Token: $token")
        if (token == null || TokenManager.isTokenExpired(token)) {
            Log.d("MetricSender", "Access token is missing or expired, attempting refresh...")
            refreshAndRetry(context) {
                sendSteps(context, steps, timestamp)
            }
            return
        }

        // Формируем тело запроса
        val metricRequest = MetricRequestDto(
            type = "steps",
            value = steps,
            timestamp = timestamp
        )

        ApiClient.authService.sendMetrics(
            metricRequest = metricRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("MetricSender", "Steps sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()
                    Log.e("MetricSender", "Failed to send steps. Code: $code")
                    Log.e("MetricSender", "Error body: $errorBody")

                    if ((code == 500 || code == 401)
                        && errorBody?.contains("JWT token is invalid") == true) {
                        Log.d("MetricSender", "Detected invalid JWT, attempting refresh...")
                        refreshAndRetry(context) {
                            sendSteps(context, steps, timestamp)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MetricSender", "Error sending steps: ${t.message}", t)

                if (t is ProtocolException && t.message?.contains("unexpected end of stream") == true) {
                    Log.e("MetricSender", "Looks like server dropped the connection. Could retry or wait.")
                }
            }
        })
    }

    fun sendHeartbeat(context: Context, heartbeat: Int, timestamp: Long) {
        val token = TokenManager.getAccessToken(context)
        Log.d("MetricSender", "Access Token: $token")
        if (token == null || TokenManager.isTokenExpired(token)) {
            Log.d("MetricSender", "Access token is missing or expired, attempting refresh...")
            refreshAndRetry(context) {
                sendHeartbeat(context, heartbeat, timestamp)
            }
            return
        }

        val metricRequest = MetricRequestDto(
            type = "heartbeat",
            value = heartbeat,
            timestamp = timestamp
        )

        ApiClient.authService.sendMetrics(
            metricRequest = metricRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("MetricSender", "Heartbeat sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()
                    Log.e("MetricSender", "Failed to send heartbeat. Code: $code")
                    Log.e("MetricSender", "Error body: $errorBody")

                    if ((code == 500 || code == 401)
                        && errorBody?.contains("JWT token is invalid") == true) {
                        Log.d("MetricSender", "Detected invalid JWT, attempting refresh...")
                        refreshAndRetry(context) {
                            sendHeartbeat(context, heartbeat, timestamp)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MetricSender", "Error sending heartbeat: ${t.message}", t)

                if (t is ProtocolException && t.message?.contains("unexpected end of stream") == true) {
                    Log.e("MetricSender", "Looks like server dropped the connection. Could retry or wait.")
                }
            }
        })
    }

    fun sendCalories(context: Context, calories: Int, timestamp: Long) {
        val token = TokenManager.getAccessToken(context)
        Log.d("MetricSender", "Access Token: $token")
        if (token == null || TokenManager.isTokenExpired(token)) {
            Log.d("MetricSender", "Access token is missing or expired, attempting refresh...")
            refreshAndRetry(context) {
                sendCalories(context, calories, timestamp)
            }
            return
        }

        val metricRequest = MetricRequestDto(
            type = "calories",
            value = calories,
            timestamp = timestamp
        )

        ApiClient.authService.sendMetrics(metricRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("MetricSender", "Calories sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()
                    Log.e("MetricSender", "Failed to send calories. Code: $code")
                    Log.e("MetricSender", "Error body: $errorBody")

                    if ((code == 500 || code == 401)
                        && errorBody?.contains("JWT token is invalid") == true) {
                        Log.d("MetricSender", "Detected invalid JWT, attempting refresh...")
                        refreshAndRetry(context) {
                            sendCalories(context, calories, timestamp)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MetricSender", "Error sending calories: ${t.message}", t)
            }
        })
    }

    fun sendActiveMinutes(context: Context, minutes: Int, timestamp: Long) {
        val token = TokenManager.getAccessToken(context)
        Log.d("MetricSender", "Access Token: $token")
        if (token == null || TokenManager.isTokenExpired(token)) {
            Log.d("MetricSender", "Access token is missing or expired, attempting refresh...")
            refreshAndRetry(context) {
                sendActiveMinutes(context, minutes, timestamp)
            }
            return
        }

        val metricRequest = MetricRequestDto(
            type = "active_minutes",
            value = minutes,
            timestamp = timestamp
        )

        ApiClient.authService.sendMetrics(metricRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("MetricSender", "Active minutes sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()
                    Log.e("MetricSender", "Failed to send active minutes. Code: $code")
                    Log.e("MetricSender", "Error body: $errorBody")

                    if ((code == 500 || code == 401)
                        && errorBody?.contains("JWT token is invalid") == true) {
                        Log.d("MetricSender", "Detected invalid JWT, attempting refresh...")
                        refreshAndRetry(context) {
                            sendActiveMinutes(context, minutes, timestamp)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MetricSender", "Error sending active minutes: ${t.message}", t)
            }
        })
    }

    fun sendCoordinates(context: Context, latitude: Double, longitude: Double, timestamp: Long) {
        val token = TokenManager.getAccessToken(context)
        Log.d("MetricSender", "Access Token: $token")

        if (token == null || TokenManager.isTokenExpired(token)) {
            Log.d("MetricSender", "Access token is missing or expired, attempting refresh...")
            refreshAndRetry(context) {
                sendCoordinates(context, latitude, longitude, timestamp)
            }
            return
        }

        val locationValue = "${latitude}:${longitude}"

        val metricRequest = MetricRequestDtoStr(
            type = "coordinates",
            value = locationValue,
            timestamp = timestamp
        )

        ApiClient.authService.sendMetricsStr(
            metricRequestStr = metricRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("MetricSender", "Coordinates sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()
                    Log.e("MetricSender", "Failed to send coordinates. Code: $code")
                    Log.e("MetricSender", "Error body: $errorBody")

                    if ((code == 500 || code == 401)
                        && errorBody?.contains("JWT token is invalid") == true) {
                        Log.d("MetricSender", "Detected invalid JWT, attempting refresh...")
                        refreshAndRetry(context) {
                            sendCoordinates(context, latitude, longitude, timestamp)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MetricSender", "Error sending coordinates: ${t.message}", t)
            }
        })
    }

    fun sendNotification(context: Context, packageName: String, title: String, message: String, timestamp: Long) {
        val token = TokenManager.getAccessToken(context)
        Log.d("MetricSender", "Access Token: $token")
        if (token == null || TokenManager.isTokenExpired(token)) {
            Log.d("MetricSender", "Access token is missing or expired, attempting refresh...")
            refreshAndRetry(context) {
                sendNotification(context, packageName, title, message, timestamp)
            }
            return
        }

        val infoMessage = "app:${packageName};title:${title};message:${message}"

        val metricRequest = MetricRequestDtoStr(
            type = "notification",
            value = infoMessage,
            timestamp = timestamp
        )

        ApiClient.authService.sendMetricsStr(
            metricRequestStr = metricRequest
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("MetricSender", "Notification sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val code = response.code()
                    Log.e("MetricSender", "Failed to send notification. Code: $code")
                    Log.e("MetricSender", "Error body: $errorBody")

                    if ((code == 500 || code == 401)
                        && errorBody?.contains("JWT token is invalid") == true) {
                        Log.d("MetricSender", "Detected invalid JWT, attempting refresh...")
                        refreshAndRetry(context) {
                            sendNotification(context, packageName, title, message, timestamp)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("MetricSender", "Error sending notification: ${t.message}", t)
            }
        })
    }

//    private fun refreshAndRetry(context: Context, onRefreshed: () -> Unit) {
//        val refreshToken = TokenManager.getRefreshToken(context)
//        Log.d("MetricSender", "Refresh token: $refreshToken")
//        if (refreshToken != null) {
//            ApiClient.authService.refresh("Bearer $refreshToken")
//                .enqueue(object : Callback<ResponseBody> {
//                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                        if (response.isSuccessful && response.body() != null) {
//                            val newAccessToken = response.body()!!.string()
//
//                            if (newAccessToken.isNotEmpty()) {
//                                val newTokens = AuthResponse(
//                                    accessToken = newAccessToken,
//                                    refreshToken = refreshToken
//                                )
//                                TokenManager.saveTokens(context, newTokens)
//                                Log.d("MetricSender", "Token refreshed successfully, retrying operation.")
//                                onRefreshed()
//                            } else {
//                                Log.e("MetricSender", "Refresh response is empty.")
//                            }
//                        } else {
//                            Log.e("MetricSender", "Failed to refresh token. Code=${response.code()}")
//                        }
//                    }
//
//                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                        Log.e("MetricSender", "Token refresh failed: ${t.message}", t)
//                    }
//                })
//        } else {
//            Log.e("MetricSender", "No refresh token available.")
//        }
//    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private fun refreshAndRetry(context: Context, onRefreshed: () -> Unit) {
        scope.launch {
            TokenManager.withTokenLock {
                val refreshToken = TokenManager.getRefreshToken(context)
                if (refreshToken != null) {
                    try {
                        val response = ApiClient.authService.refresh("Bearer $refreshToken").execute()
                        if (response.isSuccessful && response.body() != null) {
                            val newAccessToken = response.body()!!.string()
                            if (newAccessToken.isNotEmpty()) {
                                val newTokens = AuthResponse(
                                    accessToken = newAccessToken,
                                    refreshToken = refreshToken
                                )
                                TokenManager.saveTokens(context, newTokens)
                                Log.d("MetricSender", "Token refreshed successfully, retrying operation.")
                                withContext(Dispatchers.Main) {
                                    onRefreshed()
                                }
                            } else {
                                Log.e("MetricSender", "Refresh response is empty.")
                            }
                        } else {
                            Log.e("MetricSender", "Failed to refresh token. Code=${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("MetricSender", "Token refresh failed: ${e.message}", e)
                    }
                } else {
                    Log.e("MetricSender", "No refresh token available.")
                }
            }
        }
    }
}
