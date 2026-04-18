package com.example.myapplication.data.geocoding

import android.content.Context
import android.location.Geocoder
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class GeocodingRepository(context: Context) {

    private val geocoder = Geocoder(context, Locale.getDefault())

    @Suppress("DEPRECATION")
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val subLocality = address.subLocality
                    val locality = address.locality
                    val adminArea = address.adminArea
                    val addressText = when {
                        !subLocality.isNullOrBlank() && !locality.isNullOrBlank() -> "$subLocality, $locality"
                        !locality.isNullOrBlank() -> locality
                        !adminArea.isNullOrBlank() -> adminArea
                        else -> "Madrid"
                    }
                    AppLogger.d(TAG, "Geocoded ($latitude, $longitude) -> $addressText")
                    Result.success(addressText)
                } else {
                    AppLogger.e(TAG, "No address found for ($latitude, $longitude)")
                    Result.failure(Exception("No address found"))
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Geocoding failed", e)
                Result.failure(e)
            }
        }
    }

    companion object {
        private const val TAG = "GeocodingRepo"
    }
}
