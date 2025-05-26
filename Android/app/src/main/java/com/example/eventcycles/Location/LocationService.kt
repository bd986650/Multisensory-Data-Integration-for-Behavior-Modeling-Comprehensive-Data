package com.example.eventcycles

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.json.JSONObject

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    @SuppressLint("ForegroundServiceType")
    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForeground(1, createNotification())
        startLocationUpdates()
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Location Tracking", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Сбор местоположения")
            .setContentText("Идет сбор координат в фоне")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationService", "No permission")
            stopSelf()
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000)
            .setMinUpdateDistanceMeters(10f)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation
                    if (location != null) {
                        handleNewLocation(location)
                    }
                }
            },
            mainLooper
        )
    }

    private fun handleNewLocation(location: Location) {
        if (lastLocation == null || lastLocation!!.distanceTo(location) > 5) {
            // MetricSender.sendCoordinates(this, location.latitude, location.longitude, System.currentTimeMillis() / 1000)
            val json = JSONObject().apply {
                put("coordinates", "lat="+(location.latitude).toString()+",lon="+(location.longitude).toString())
                put("timestamp", System.currentTimeMillis())
                put("latitude", location.latitude)
                put("longitude", location.longitude)
            }
            Log.d("LocationService", "New Location: $json")
            lastLocation = location
        } else {
            Log.d("LocationService", "Same location, skipping. Last JSON: ${lastLocation?.let {
                JSONObject().apply {
                    put("latitude", it.latitude)
                    put("longitude", it.longitude)
                    put("timestamp", it.time)
                }
            }}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
