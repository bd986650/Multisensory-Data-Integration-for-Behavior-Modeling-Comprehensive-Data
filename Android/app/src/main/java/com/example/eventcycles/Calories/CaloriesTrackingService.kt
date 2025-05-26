package com.example.eventcycles

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.*
import java.util.concurrent.TimeUnit

class CaloriesTrackingService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 60_000 // каждые 1 минуту

    companion object {
        private const val CHANNEL_ID = "calories_tracking_channel"
        private const val NOTIFICATION_ID = 1004
    }

    private val runnable = object : Runnable {
        override fun run() {
            readCaloriesData()
            handler.postDelayed(this, interval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun readCaloriesData() {
        val end = Calendar.getInstance()
        val start = end.clone() as Calendar
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            .setTimeRange(start.timeInMillis, end.timeInMillis, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, GoogleFitHelper.getFitnessOptions())
        Fitness.getHistoryClient(this, account)
            .readData(request)
            .addOnSuccessListener { response ->
                var totalCalories = 0f
                response.buckets.flatMap { it.dataSets }.flatMap { it.dataPoints }.forEach {
                    totalCalories += it.getValue(DataType.AGGREGATE_CALORIES_EXPENDED.fields[0]).asFloat()
                }

                val intCalories = totalCalories.toInt()

                Log.d("CaloriesTrackingService", "Total calories today: $intCalories")

                val prefs = getSharedPreferences("calories_prefs", Context.MODE_PRIVATE)
                prefs.edit().putInt("last_calories", totalCalories.toInt()).apply()
            }
            .addOnFailureListener { e ->
                Log.e("CaloriesTrackingService", "Failed to read calories data", e)
            }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Calories Tracking Active")
            .setContentText("Tracking your burned calories from Google Fit.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Calories Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
