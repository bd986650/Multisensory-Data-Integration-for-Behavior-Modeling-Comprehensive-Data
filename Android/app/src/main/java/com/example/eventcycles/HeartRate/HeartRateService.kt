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
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class HeartRateService : Service() {

    private var lastHeartbeatCount = -1
    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 1 * 60 * 1000L // каждые 1 минут

    companion object {
        private const val CHANNEL_ID = "heart_rate_channel"
        private const val NOTIFICATION_ID = 2001
    }

    private val runnable = object : Runnable {
        override fun run() {
            readHeartRate()
            handler.postDelayed(this, interval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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

    private fun readHeartRate() {
        val end = Calendar.getInstance()
        val start = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, -1) // последние 60 минут
        }

        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(start.timeInMillis, end.timeInMillis, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.HOURS)
            .build()

        val account = GoogleSignIn.getAccountForExtension(this, GoogleFitHelper.getFitnessOptions())
        Fitness.getHistoryClient(this, account)
            .readData(request)
            .addOnSuccessListener { response ->
                val average = response.buckets
                    .flatMap { it.dataSets }
                    .flatMap { it.dataPoints }
                    .mapNotNull { it.getValue(DataType.AGGREGATE_HEART_RATE_SUMMARY.fields[1]).asFloat() }
                    .average()

                val bpm = if (average.isNaN()) 0 else average.toInt()

                if (bpm != lastHeartbeatCount) {
                    lastHeartbeatCount = bpm
                    // MetricSender.sendHeartbeat(this, bpm, System.currentTimeMillis() / 1000)
                    val json = JSONObject().apply {
                        put("heartbeat", bpm)
                        put("timestamp", System.currentTimeMillis())
                    }
                    Log.d("HeartRateService", "Heart rate: $json")

                    val prefs = getSharedPreferences("heart_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("last_heart_rate", bpm).apply()
                }
            }
            .addOnFailureListener { e ->
                Log.e("HeartRateService", "Failed to read heart rate", e)
            }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Heart Rate Tracking")
            .setContentText("Monitoring heart rate from Google Fit.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Heart Rate Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}
