package com.lex.qr.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import javax.inject.Inject

class GeolocationClient {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    var lastLocation: String? = null

    fun startLocationUpdates(context: Context) {
        if (android.os.Build.MANUFACTURER.equals("HUAWEI", ignoreCase = true)) {
            useLocationManager(context)
        } else {
            useFusedLocationProvider(context)
        }
    }
    private fun useLocationManager(context: Context) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = LocationListener { location -> lastLocation = "${location.latitude}|${location.longitude}" }

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1001
                )
            } else {
                return
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 10f, locationListener)
        }
    }

    private fun useFusedLocationProvider(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.locations.forEach { location ->
                    lastLocation = "${location.latitude}|${location.longitude}"
                }
            }
        }

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    1001
                )
            } else {
                return
            }
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
    fun checkGps(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
            return false
        }
        return true
    }
}