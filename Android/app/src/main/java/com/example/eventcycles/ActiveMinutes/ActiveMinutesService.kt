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

class ActiveMinutesService : Service() {

    private var lastActiveMinutes = -1
    private val handler = Handler(Looper.getMainLooper())
    private val interval: Long = 60_000 // 1 минута

    companion object {
        private const val CHANNEL_ID = "active_minutes_channel"
        private const val NOTIFICATION_ID = 1002
    }

    private val runnable = object : Runnable {
        override fun run() {
            readActiveMinutes()
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

    private fun readActiveMinutes() {
        val end = Calendar.getInstance()
        val start = end.clone() as Calendar
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        val request = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_MOVE_MINUTES, DataType.AGGREGATE_MOVE_MINUTES)
            .setTimeRange(start.timeInMillis, end.timeInMillis, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()


        val account = GoogleSignIn.getAccountForExtension(this, GoogleFitHelper.getFitnessOptions())
        if (!GoogleSignIn.hasPermissions(account, GoogleFitHelper.getFitnessOptions())) {
            Log.e("ActiveMinutesService", "Google Fit permissions not granted")
            return
        }

        Fitness.getHistoryClient(this, account)
            .readData(request)
            .addOnSuccessListener { response ->
                Log.d("ActiveMinutesService", "Read success. Buckets count: ${response.buckets.size}")
                val dataPoints = response.buckets.flatMap { it.dataSets }.flatMap { it.dataPoints }

                Log.d("ActiveMinutesService", "Data points found: ${dataPoints.size}")
                dataPoints.forEachIndexed { index, point ->
                    Log.d("ActiveMinutesService", "[7.$index] Point: ${point.getValue(DataType.AGGREGATE_MOVE_MINUTES.fields[0])}")
                }

                val activeMinutes = dataPoints.sumOf {
                    it.getValue(DataType.AGGREGATE_MOVE_MINUTES.fields[0]).asInt()
                }

                Log.d("ActiveMinutesService", "Total Active Minutes: $activeMinutes")

                if (activeMinutes != lastActiveMinutes) {
                    Log.d("ActiveMinutesService", "New value detected. Saving to prefs.")
                    lastActiveMinutes = activeMinutes
                    val prefs = getSharedPreferences("active_minutes_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putInt("last_active_minutes", activeMinutes).apply()
                } else {
                    Log.d("ActiveMinutesService", "Value unchanged. Not updating.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ActiveMinutesService", "Failed to read data from Google Fit", e)
            }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Active Minutes Tracking Active")
            .setContentText("Tracking your active minutes from Google Fit.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Active Minutes Service"
            val descriptionText = "Tracks active minutes in background"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
