package com.example.eventcycles

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_ACTIVITY_RECOGNITION = 100
        private const val REQUEST_CODE_GOOGLE_FIT = 101
        private const val REQUEST_LOCATION_PERMISSION = 102
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_notifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_steps).setOnClickListener {
            startActivity(Intent(this, StepsActivity::class.java))
        }

        findViewById<Button>(R.id.btn_show_map).setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        findViewById<Button>(R.id.btn_heart_rate).setOnClickListener {
            startActivity(Intent(this, HeartRateActivity::class.java))
        }

        findViewById<Button>(R.id.btn_active_minutes).setOnClickListener {
            startActivity(Intent(this, ActiveMinutesActivity::class.java))
        }

        findViewById<Button>(R.id.btn_calories).setOnClickListener {
            startActivity(Intent(this, CaloriesActivity::class.java))
        }

        if (!isNotificationAccessGranted()) {
            Toast.makeText(this, "Allow access to notifications", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        checkLocationPermissionsAndStartService()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                REQUEST_CODE_ACTIVITY_RECOGNITION
            )
        }

        val fitnessOptions = GoogleFitHelper.getFitnessOptions()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                REQUEST_CODE_GOOGLE_FIT,
                account,
                fitnessOptions
            )
        } else {
            startStepCounterService()
            startHeartRateService()
            startActiveMinutesService()
            startCaloriesTrackingService()
        }
    }

    private fun startHeartRateService() {
        val intent = Intent(this, HeartRateService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun startStepCounterService() {
        val intent = Intent(this, StepCounterService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun startActiveMinutesService() {
        val intent = Intent(this, ActiveMinutesService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun startCaloriesTrackingService() {
        val intent = Intent(this, CaloriesTrackingService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GOOGLE_FIT) {
            if (resultCode == RESULT_OK) {
                startStepCounterService()
                startHeartRateService()
                startActiveMinutesService()
                startCaloriesTrackingService()
            } else {
                Toast.makeText(this, "Google Fit permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNotificationAccessGranted(): Boolean {
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }

    private fun checkLocationPermissionsAndStartService() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && // Android 14+
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_LOCATION_PERMISSION)
        } else {
            startLocationService()
        }
    }

    private fun startLocationService() {
        val intent = Intent(this, LocationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}
