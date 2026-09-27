package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {

    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float? = null,
        val isMock: Boolean = false,
        val isFallback: Boolean = false,
        val sourceName: String = "GPS"
    )

    // Default reference location (Highway BR-101 Brazil sample)
    val DEFAULT_LOCATION = LocationResult(
        latitude = -23.550520,
        longitude = -46.633308,
        accuracy = null,
        isMock = true,
        isFallback = true,
        sourceName = "Padrão (São Paulo SP)"
    )

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (LocationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val accuracyVal = if (location.hasAccuracy()) location.accuracy else null
                    onSuccess(
                        LocationResult(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = accuracyVal,
                            isMock = location.isFromMockProvider,
                            sourceName = "GPS Fused Location"
                        )
                    )
                } else {
                    // Try last known location or LocationManager fallback
                    tryLocationManagerFallback(context, onSuccess, onError)
                }
            }.addOnFailureListener {
                tryLocationManagerFallback(context, onSuccess, onError)
            }
        } catch (e: Exception) {
            tryLocationManagerFallback(context, onSuccess, onError)
        }
    }

    @SuppressLint("MissingPermission")
    private fun tryLocationManagerFallback(
        context: Context,
        onSuccess: (LocationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                onSuccess(DEFAULT_LOCATION)
                return
            }

            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val bestLocation = gpsLocation ?: networkLocation

            if (bestLocation != null) {
                val accuracyVal = if (bestLocation.hasAccuracy()) bestLocation.accuracy else null
                onSuccess(
                    LocationResult(
                        latitude = bestLocation.latitude,
                        longitude = bestLocation.longitude,
                        accuracy = accuracyVal,
                        sourceName = if (gpsLocation != null) "GPS Provider" else "Network Provider"
                    )
                )
            } else {
                // If no hardware GPS available (e.g. running on headless container/emulator), use default field coordinates
                onSuccess(DEFAULT_LOCATION)
            }
        } catch (e: Exception) {
            onSuccess(DEFAULT_LOCATION)
        }
    }
}
