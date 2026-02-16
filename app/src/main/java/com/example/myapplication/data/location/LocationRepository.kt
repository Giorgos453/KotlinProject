package com.example.myapplication.data.location

import android.annotation.SuppressLint
import android.location.Location
import com.example.myapplication.util.AppLogger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationRepository(
    private val fusedLocationClient: FusedLocationProviderClient
) {
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

    companion object {
        private const val TAG = "LocationRepo"
    }
}
