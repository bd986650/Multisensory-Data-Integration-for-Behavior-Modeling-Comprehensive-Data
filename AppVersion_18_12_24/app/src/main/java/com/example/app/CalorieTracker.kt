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
class CalorieTracker(private val context: Context) {

    companion object {
        const val TAG = "CalorieTracker"
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    private val account
        get() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    fun hasPermissions(): Boolean {
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    fun requestPermissions(activity: MainActivity, requestCode: Int) {
        GoogleSignIn.requestPermissions(activity, requestCode, account, fitnessOptions)
    }

    fun readCalories(onCaloriesRead: (Float) -> Unit) {
        Log.d(TAG, "Checking Google Fit account...")
        if (account == null) {
            Toast.makeText(context, "Не удалось получить аккаунт Google.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Google account is null.")
            onCaloriesRead(0f)
            return
        }

        Log.d(TAG, "Account found, initiating calorie read request...")

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1) // Получаем калории за последние 24 часа
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                Log.d(TAG, "Calorie data read successfully.")
                var totalCalories = 0f
                for (bucket in response.buckets) {
                    val dataSet = bucket.getDataSet(DataType.AGGREGATE_CALORIES_EXPENDED)
                    if (dataSet != null) {
                        for (dp in dataSet.dataPoints) {
                            totalCalories += dp.getValue(Field.FIELD_CALORIES).asFloat()
                        }
                    }
                }
                Log.d(TAG, "Total calories: $totalCalories")
                onCaloriesRead(totalCalories)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading Google Fit data", e)
                Toast.makeText(context, "Ошибка чтения данных Google Fit: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                onCaloriesRead(0f)
            }
    }
}
