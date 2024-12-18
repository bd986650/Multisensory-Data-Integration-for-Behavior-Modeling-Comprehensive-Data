@file:Suppress("DEPRECATION")

package com.example.app

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.SessionReadRequest
import com.google.android.gms.fitness.FitnessOptions
import java.util.concurrent.TimeUnit

class SleepTracker(private val context: Context) {

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .build()

    // Проверка наличия разрешений
    fun hasPermissions(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    // Запрос разрешений
    fun requestPermissions(activity: MainActivity, requestCode: Int) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        GoogleSignIn.requestPermissions(activity, requestCode, account, fitnessOptions)
    }

    // Чтение данных о сне
    fun readSleepData(callback: (sleepDuration: Long) -> Unit) {
        val endTime = System.currentTimeMillis() // Текущий момент
        val startTime = endTime - TimeUnit.DAYS.toMillis(1) // За последние 24 часа

        // Создаем запрос на чтение сессий
        val readRequest = SessionReadRequest.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS) // Устанавливаем интервал времени
            .read(DataType.TYPE_SLEEP_SEGMENT) // Чтение данных о сне
            .enableServerQueries() // Если нужно получать данные с сервера
            .build()

        // Получаем данные о сессиях
        Fitness.getSessionsClient(context, GoogleSignIn.getLastSignedInAccount(context)!!)
            .readSession(readRequest)
            .addOnSuccessListener { response ->
                var totalSleepDuration: Long = 0

                // Проходим по всем сессиям
                for (session in response.sessions) {
                    // Получаем данные из сессии
                    val dataSets = response.getDataSet(session)
                    for (dataSet in dataSets) {
                        for (dataPoint in dataSet.dataPoints) {
                            // Вычисляем продолжительность сна
                            val sleepStartTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                            val sleepEndTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS)

                            // Добавляем продолжительность сна в общий итог
                            totalSleepDuration += sleepEndTime - sleepStartTime
                        }
                    }
                }

                // Отправляем общую продолжительность сна в колбэк
                callback(totalSleepDuration)
            }
            .addOnFailureListener { e ->
                Log.e("SleepTracker", "Ошибка чтения данных о сне", e)
                callback(0) // В случае ошибки возвращаем 0
            }
    }
}
