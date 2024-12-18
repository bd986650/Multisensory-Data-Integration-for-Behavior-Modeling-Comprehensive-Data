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

class StepCounter(private val context: Context) {

    companion object {
        const val TAG = "StepCounter"
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    private val account
        get() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    fun hasPermissions(): Boolean {
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    fun requestPermissions(activity: MainActivity, requestCode: Int) {
        GoogleSignIn.requestPermissions(activity, requestCode, account, fitnessOptions)
    }

    fun readSteps(onStepsRead: (Int) -> Unit) {
        Log.d(TAG, "Checking Google Fit account...")
        if (account == null) {
            Toast.makeText(context, "Не удалось получить аккаунт Google.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Google account is null.")
            onStepsRead(0) // завершение с нулевыми шагами в случае ошибки
            return
        }

        Log.d(TAG, "Account found, initiating step read request...")

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                Log.d(TAG, "Step data read successfully.")
                var totalSteps = 0
                for (bucket in response.buckets) {
                    val dataSet = bucket.getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA)
                    if (dataSet != null) {
                        for (dp in dataSet.dataPoints) {
                            totalSteps += dp.getValue(Field.FIELD_STEPS).asInt()
                        }
                    }
                }
                Log.d(TAG, "Total steps: $totalSteps")
                onStepsRead(totalSteps)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading Google Fit data", e) // Запись полного стека ошибки в лог
                Toast.makeText(context, "Ошибка чтения данных Google Fit: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                onStepsRead(0)
            }
    }
}
