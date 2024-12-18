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
class DistanceTracker(private val context: Context) {

    companion object {
        const val TAG = "DistanceTracker"
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    private val account
        get() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    fun hasPermissions(): Boolean {
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    fun requestPermissions(activity: MainActivity, requestCode: Int) {
        GoogleSignIn.requestPermissions(activity, requestCode, account, fitnessOptions)
    }

    fun readDistance(onDistanceRead: (Float) -> Unit) {
        Log.d(TAG, "Checking Google Fit account...")
        if (account == null) {
            Toast.makeText(context, "Не удалось получить аккаунт Google.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Google account is null.")
            onDistanceRead(0f) // Если ошибка, возвращаем 0
            return
        }

        Log.d(TAG, "Account found, initiating distance read request...")

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1) // В течение последних суток

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                Log.d(TAG, "Distance data read successfully.")
                var totalDistance = 0f
                for (bucket in response.buckets) {
                    val dataSet = bucket.getDataSet(DataType.AGGREGATE_DISTANCE_DELTA)
                    if (dataSet != null) {
                        for (dp in dataSet.dataPoints) {
                            totalDistance += dp.getValue(Field.FIELD_DISTANCE).asFloat()
                        }
                    }
                }
                Log.d(TAG, "Total distance: $totalDistance meters")
                onDistanceRead(totalDistance)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading Google Fit data", e)
                Toast.makeText(context, "Ошибка чтения данных Google Fit: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                onDistanceRead(0f)
            }
    }
}
