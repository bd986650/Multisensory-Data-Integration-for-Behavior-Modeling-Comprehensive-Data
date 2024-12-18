@file:Suppress("DEPRECATION")

package com.example.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.concurrent.TimeUnit

class DataManager(private val context: Context) {

    companion object {
        const val TAG = "DataManager"
        private const val PREFS_NAME = "HealthData"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val heartRates = mutableListOf<Float>()
    private val stepHistory = mutableListOf<Int>()
    private val calorieHistory = mutableListOf<Float>()
    private val distanceHistory = mutableListOf<Float>()

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .build()

    private val account
        get() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    // Метод для загрузки сохраненной истории сердцебиений из SharedPreferences
    fun loadHeartRateHistory(): List<Float> {
        val savedHeartRates = sharedPreferences.getString("heartRateHistory", null)
        return savedHeartRates?.split(",")?.map { it.toFloat() } ?: emptyList()
    }

    // Метод для сохранения истории сердцебиений в SharedPreferences
    private fun saveHeartRateHistory() {
        val heartRateHistoryString = heartRates.joinToString(",")  // Преобразуем список в строку
        sharedPreferences.edit().putString("heartRateHistory", heartRateHistoryString).apply()
    }


    // Метод для обновления истории сердцебиений
    fun updateHeartRateHistory(newHeartRate: Float) {
        if (heartRates.size >= 5) {
            heartRates.removeAt(0)  // Удаляем самое старое значение, если список больше 5
        }
        heartRates.add(newHeartRate)  // Добавляем новое значение в список
        saveHeartRateHistory()  // Сохраняем обновленную историю в SharedPreferences
    }


    // Метод для получения данных о сердцебиении
    fun getHeartRateData(onHeartRateRead: (Float) -> Unit) {
        if (account == null) {
            Log.e(TAG, "Google account is null.")
            onHeartRateRead(0f)
            return
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.MINUTES.toMillis(1)
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                var latestHeartRate: Float? = null
                for (dataSet in response.dataSets) {
                    for (dp in dataSet.dataPoints) {
                        latestHeartRate = dp.getValue(Field.FIELD_BPM).asFloat()
                    }
                }
                onHeartRateRead(latestHeartRate ?: 0f)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading heart rate data", e)
                onHeartRateRead(0f)
            }
    }

    fun loadStepHistory(): List<Int> {
        val savedStepHistory = sharedPreferences.getString("stepHistory", null)
        return savedStepHistory?.split(",")?.map { it.toInt() } ?: emptyList()
    }

    private fun saveStepHistory() {
        val stepHistoryString = stepHistory.joinToString(",")
        sharedPreferences.edit().putString("stepHistory", stepHistoryString).apply()
    }

    fun updateStepHistory(newSteps: Int) {
        if (stepHistory.size >= 5) {
            stepHistory.removeAt(0)  // Удаляем самое старое значение, если больше 5
        }
        stepHistory.add(newSteps)  // Добавляем новое значение
        saveStepHistory()  // Сохраняем обновленную историю
    }


    // Можно добавить методы для других типов данных, например шаги, калории, дистанция и активные минуты
    // Например, метод для получения данных о шагах:

    fun getStepData(onStepsRead: (Int) -> Unit) {
        if (account == null) {
            Log.e(TAG, "Google account is null.")
            onStepsRead(0)
            return
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)

        // Используем агрегирование с бакетами по времени
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.HOURS) // Агрегация по часам
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                var totalSteps = 0
                for (bucket in response.buckets) {
                    val dataSet = bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA)
                    if (dataSet != null) {
                        for (dp in dataSet.dataPoints) {
                            totalSteps += dp.getValue(Field.FIELD_STEPS).asInt()
                        }
                    }
                }
                onStepsRead(totalSteps)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading step data", e)
                onStepsRead(0)
            }
    }

    fun loadCalorieHistory(): List<Float> {
        val savedCalories = sharedPreferences.getString("calorieHistory", null)
        return savedCalories?.split(",")?.map { it.toFloat() } ?: emptyList()
    }

    private fun saveCalorieHistory() {
        val calorieHistoryString = calorieHistory.joinToString(",")
        sharedPreferences.edit().putString("calorieHistory", calorieHistoryString).apply()
    }

    fun updateCalorieHistory(newCalories: Float) {
        if (calorieHistory.size >= 5) {
            calorieHistory.removeAt(0) // Удаляем самое старое значение, если список больше 5
        }
        calorieHistory.add(newCalories) // Добавляем новое значение
        saveCalorieHistory() // Сохраняем обновленную историю
    }

    fun getCalorieData(onCaloriesRead: (Float) -> Unit) {
        val calorieTracker = CalorieTracker(context)
        calorieTracker.readCalories { newCalories ->
            onCaloriesRead(newCalories)
        }
    }

    fun loadDistanceHistory(): List<Float> {
        val savedDistanceHistory = sharedPreferences.getString("distanceHistory", null)
        return savedDistanceHistory?.split(",")?.map { it.toFloat() } ?: emptyList()
    }

    private fun saveDistanceHistory() {
        val distanceHistoryString = distanceHistory.joinToString(",")
        sharedPreferences.edit().putString("distanceHistory", distanceHistoryString).apply()
    }

    fun updateDistanceHistory(newDistance: Float) {
        if (distanceHistory.size >= 5) {
            distanceHistory.removeAt(0) // Удаляем самое старое значение, если больше 5
        }
        distanceHistory.add(newDistance) // Добавляем новое значение
        saveDistanceHistory() // Сохраняем обновленную историю
    }

    fun getDistanceData(onDistanceRead: (Float) -> Unit) {
        val distanceTracker = DistanceTracker(context)
        distanceTracker.readDistance { newDistance ->
            onDistanceRead(newDistance)
        }
    }


    fun generateHealthSummary(onSummaryReady: (HealthSummary) -> Unit) {
        val summary = HealthSummary(
            steps = loadStepHistory().lastOrNull() ?: 0,
            stepHistory = loadStepHistory(),
            heartRate = loadHeartRateHistory().lastOrNull() ?: 0f,
            heartRateHistory = loadHeartRateHistory(),
            calories = loadCalorieHistory().lastOrNull() ?: 0f,
            calorieHistory = loadCalorieHistory(),
            distance = loadDistanceHistory().lastOrNull() ?: 0f,
            distanceHistory = loadDistanceHistory()
            //activeMinutes = getActiveMinutes(),
            //sleepDuration = getSleepDuration()
        )
        onSummaryReady(summary)
    }


}
