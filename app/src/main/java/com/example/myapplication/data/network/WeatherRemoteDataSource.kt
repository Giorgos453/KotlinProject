package com.example.myapplication.data.network

import com.example.myapplication.data.network.model.ForecastResponse
import com.example.myapplication.data.network.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps all Retrofit calls.
 * Returns Result<T> — never raw exceptions to the ViewModel.
 * All network operations run on Dispatchers.IO.
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

    suspend fun getForecast(lat: Double, lon: Double, apiKey: String): Result<ForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = service.getForecast(lat, lon, apiKey)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
