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
                    val addressText = address.getAddressLine(0) ?: buildString {
                        address.thoroughfare?.let { append(it) }
                        address.locality?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
                        address.countryName?.let {
                            if (isNotEmpty()) append(", ")
                            append(it)
                        }
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
