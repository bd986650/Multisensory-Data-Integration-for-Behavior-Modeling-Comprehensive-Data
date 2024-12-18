package com.example.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        setContentView(R.layout.activity_map)

        // Initialize the MapView
        mapView = findViewById(R.id.map)
        mapView.setMultiTouchControls(true)

        // Set the starting point on the map
        val startPoint = GeoPoint(48.8588443, 2.2943506) // Example: Paris coordinates
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(startPoint)

        // Request location permission if not already granted
        requestLocationPermission()

        // Add compass and scale bar overlays
        addCompassOverlay()
        addScaleBarOverlay()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            enableLocationOverlay()
        }
    }

    private fun enableLocationOverlay() {
        // Create the MyLocationNewOverlay
        locationOverlay = MyLocationNewOverlay(mapView)
        locationOverlay.enableMyLocation() // Enable location
        mapView.overlays.add(locationOverlay)

        // Center the map on the user's current location when it is available
        locationOverlay.enableFollowLocation() // Automatically follow the user's location
    }

    private fun addCompassOverlay() {
        val compassOverlay = CompassOverlay(this, mapView)
        compassOverlay.enableCompass()
        mapView.overlays.add(compassOverlay)
    }

    private fun addScaleBarOverlay() {
        val scaleBarOverlay = ScaleBarOverlay(mapView)
        mapView.overlays.add(scaleBarOverlay)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume() // Important for correct map display
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause() // Important for correct map display
    }
}
