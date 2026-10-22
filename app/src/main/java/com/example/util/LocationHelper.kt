package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.LocationData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone

object LocationHelper {

    private const val TAG = "LocationHelper"

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(context: Context): LocationData? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted")
            return@withContext null
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            // Try high accuracy current location
            val locationTask = fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cts.token
            )

            val location = com.google.android.gms.tasks.Tasks.await(locationTask)
                ?: run {
                    // Fallback to last known location
                    val lastTask = fusedClient.lastLocation
                    com.google.android.gms.tasks.Tasks.await(lastTask)
                }

            if (location == null) {
                Log.w(TAG, "FusedLocationProvider returned null location")
                return@withContext null
            }

            val lat = location.latitude
            val lng = location.longitude
            val timeZoneId = TimeZone.getDefault().id

            // Reverse geocode to human-readable city and country
            val (city, country) = reverseGeocode(context, lat, lng)

            return@withContext LocationData(
                cityName = city,
                country = country,
                latitude = lat,
                longitude = lng,
                timeZoneId = timeZoneId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting GPS location", e)
            return@withContext null
        }
    }

    private fun reverseGeocode(context: Context, lat: Double, lng: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val city = addr.locality
                    ?: addr.subAdminArea
                    ?: addr.adminArea
                    ?: "GPS Location"
                val country = addr.countryName ?: "Current Area"
                Pair(city, country)
            } else {
                Pair(String.format(Locale.getDefault(), "GPS (%.2f, %.2f)", lat, lng), "Detected Location")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder failed: ${e.message}")
            Pair(String.format(Locale.getDefault(), "GPS (%.2f, %.2f)", lat, lng), "Detected Location")
        }
    }
}
