package com.example.eventcycles

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class StepCounterService : Service() {

    private var lastStepCount = -1
    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 60_000 // 1 минута

    companion object {
        private const val CHANNEL_ID = "step_counter_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val runnable = object : Runnable {
        override fun run() {
            readStepCount()
            handler.postDelayed(this, interval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun readStepCount() {
        val end = Calendar.getInstance()
        val start = end.clone() as Calendar
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(start.timeInMillis, end.timeInMillis, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, GoogleFitHelper.getFitnessOptions())
        Fitness.getHistoryClient(this, account)
            .readData(request)
            .addOnSuccessListener { response ->
                val steps = response.buckets
                    .flatMap { it.dataSets }
                    .flatMap { it.dataPoints }
                    .sumOf { it.getValue(DataType.AGGREGATE_STEP_COUNT_DELTA.fields[0]).asInt() }

                if (steps != lastStepCount) {
                    lastStepCount = steps
                    // MetricSender.sendSteps(this, steps, System.currentTimeMillis() / 1000)
                    val json = JSONObject().apply {
                        put("steps", steps)
                        put("timestamp", System.currentTimeMillis())
                    }
                    Log.d("StepCounterService", "Steps changed: $json")

                    val prefs = getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("last_steps", steps).apply()
                }
            }
            .addOnFailureListener { e ->
                Log.e("StepCounterService", "Failed to read steps", e)
            }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Tracking Active")
            .setContentText("Tracking your steps from Google Fit.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Step Counter Service"
            val descriptionText = "Tracks steps in background"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
