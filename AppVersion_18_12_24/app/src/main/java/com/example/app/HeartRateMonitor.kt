@file:Suppress("DEPRECATION")

package com.example.app

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class HeartRateMonitor(private val context: Context) {

    companion object {
        const val TAG = "HeartRateMonitor"
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()

    private val account
        get() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)


    fun readHeartRate(onHeartRateRead: (Float) -> Unit) {
        Log.d(TAG, "Checking Google Fit account...")
        if (account == null) {
            Toast.makeText(context, "Не удалось получить аккаунт Google.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Google account is null.")
            return
        }

        Log.d(TAG, "Account found, initiating heart rate read request...")

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.MINUTES.toMillis(1) // последний 1 минуту
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM) // Чтение только сердцебиения
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                Log.d(TAG, "Heart rate data read successfully.")
                var latestHeartRate: Float? = null

                for (dataSet in response.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        latestHeartRate = dp.getValue(Field.FIELD_BPM).asFloat()
                    }
                }

                if (latestHeartRate != null) {
                    Log.d(TAG, "Latest heart rate: $latestHeartRate")
                    onHeartRateRead(latestHeartRate)
                } else {
                    Log.d(TAG, "No heart rate data available.")
                    onHeartRateRead(0f) // Если данных нет, передаем 0
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading Google Fit data.", e)
                onHeartRateRead(0f) // Обработка ошибок
            }
    }
}
