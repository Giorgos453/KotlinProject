package com.example.myapplication.data.network

import com.example.myapplication.data.network.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Kapselt alle Retrofit-Aufrufe.
 * Gibt Result<T> zurueck – niemals rohe Exceptions an den ViewModel.
 * Alle Netzwerkoperationen laufen auf Dispatchers.IO.
 */
class WeatherRemoteDataSource(
    private val service: WeatherApiService = RetrofitClient.weatherService
) {

    suspend fun getWeather(lat: Double, lon: Double, apiKey: String): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getCurrentWeather(lat, lon, apiKey)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
