package com.example.myapplication.data.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.example.myapplication.util.AppLogger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// NOTE FOR DEVELOPERS: The Android Emulator defaults to Googleplex (USA) as simulated location.
// To test with Madrid: Extended Controls (three dots on emulator) -> Location -> set coordinates
// to 40.4165, -3.7026 or send a route to simulate movement.

class LocationRepository(
    private val fusedLocationClient: FusedLocationProviderClient
) {
    /** Returns the current location as a single result. */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<Location> {
        return try {
            val cancellationToken = CancellationTokenSource()
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
            if (location != null) {
                AppLogger.d(TAG, "Location: ${location.latitude}, ${location.longitude}")
                Result.success(location)
            } else {
                AppLogger.e(TAG, "Location was null")
                Result.failure(Exception("Unable to get location"))
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get location", e)
            Result.failure(e)
        }
    }

    /**
     * Emits continuous location updates.
     * Update interval: 5 000 ms — minimum displacement: 5 m.
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        )
            .setMinUpdateDistanceMeters(MIN_DISPLACEMENT_M)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    AppLogger.d(TAG, "Update: ${location.latitude}, ${location.longitude}")
                    trySend(location)
                }
            }
        }

        AppLogger.i(TAG, "Starting location updates (interval=${UPDATE_INTERVAL_MS}ms, displacement=${MIN_DISPLACEMENT_M}m)")
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose {
            AppLogger.i(TAG, "Stopping location updates")
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    companion object {
        private const val TAG = "LocationRepo"
        private const val UPDATE_INTERVAL_MS = 5000L   // 5 seconds
        private const val MIN_DISPLACEMENT_M = 5f      // 5 metres
    }
}
