package com.example.eventcycles

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, getPreferences(MODE_PRIVATE))
        setContentView(R.layout.activity_map)

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkAndShowUserLocation()
    }

    private fun checkAndShowUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            showUserLocation()
        }
    }

    private fun showUserLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userPoint = GeoPoint(location.latitude, location.longitude)
                map.controller.setZoom(17.0)
                map.controller.setCenter(userPoint)

                val marker = Marker(map)
                marker.position = userPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Вы здесь"
                map.overlays.add(marker)
            } else {
                Toast.makeText(this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            showUserLocation()
        } else {
            Toast.makeText(this, "Разрешение на геолокацию не предоставлено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
