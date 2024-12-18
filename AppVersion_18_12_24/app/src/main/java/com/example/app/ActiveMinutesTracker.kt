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
class ActiveMinutesTracker(private val context: Context) {

    companion object {
        const val TAG = "ActiveMinutesTracker"
    }

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
        .build()

    private val account
        get() = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

    fun hasPermissions(): Boolean {
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }

    fun requestPermissions(activity: MainActivity, requestCode: Int) {
        GoogleSignIn.requestPermissions(activity, requestCode, account, fitnessOptions)
    }

    fun readActiveMinutes(onActiveMinutesRead: (Int) -> Unit) {
        if (account == null) {
            Toast.makeText(context, "Не удалось получить аккаунт Google.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Google account is null.")
            onActiveMinutesRead(0)
            return
        }

        Log.d(TAG, "Account found, initiating active minutes read request...")

        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_MOVE_MINUTES, DataType.AGGREGATE_MOVE_MINUTES)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account!!)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                Log.d(TAG, "Active minutes data read successfully.")
                var totalActiveMinutes = 0
                for (bucket in response.buckets) {
                    val dataSet = bucket.getDataSet(DataType.AGGREGATE_MOVE_MINUTES)
                    if (dataSet != null) {
                        for (dp in dataSet.dataPoints) {
                            totalActiveMinutes += dp.getValue(Field.FIELD_DURATION).asInt()
                        }
                    }
                }
                Log.d(TAG, "Total active minutes: $totalActiveMinutes")
                onActiveMinutesRead(totalActiveMinutes)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error reading Google Fit data", e)
                Toast.makeText(context, "Ошибка чтения данных Google Fit: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                onActiveMinutesRead(0)
            }
    }
}
