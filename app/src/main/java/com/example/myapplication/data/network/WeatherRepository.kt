package com.example.myapplication.data.network

import com.example.myapplication.data.database.dao.WeatherCacheDao
import com.example.myapplication.data.database.entity.WeatherCacheEntity
import com.example.myapplication.data.network.model.ForecastResponse
import com.example.myapplication.data.security.ApiKeyManager
import com.example.myapplication.util.AppLogger

// cache-first strategy: check cache by coordinates (<10 min), fall back to API on miss/expiry
class WeatherRepository(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val weatherCacheDao: WeatherCacheDao,
    private val apiKeyManager: ApiKeyManager
) {

    suspend fun getWeather(
        lat: Double,
        lon: Double,
        forceRefresh: Boolean = false
    ): Result<WeatherCacheEntity> {
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return Result.failure(NoApiKeyException())
        }

        if (!forceRefresh) {
            val cached = weatherCacheDao.getByLocation(lat, lon)
            if (cached != null && !cached.isExpired) {
                AppLogger.d(TAG, "Cache hit for ($lat, $lon)")
                return Result.success(cached)
            }
        }

        AppLogger.d(TAG, "Fetching weather from API for ($lat, $lon)")
        val result = remoteDataSource.getWeather(lat, lon, apiKey)

        return result.fold(
            onSuccess = { response ->
                val entity = WeatherCacheEntity(
                    latitude = response.coord?.lat ?: lat,
                    longitude = response.coord?.lon ?: lon,
                    cityName = response.cityName,
                    temperature = response.main.temp,
                    feelsLike = response.main.feelsLike,
                    humidity = response.main.humidity,
                    pressure = response.main.pressure,
                    description = response.weather.firstOrNull()?.description ?: "N/A",
                    iconCode = response.weather.firstOrNull()?.icon ?: "01d",
                    windSpeed = response.wind?.speed ?: 0.0
                )
                weatherCacheDao.insert(entity)
                AppLogger.i(TAG, "Weather cached for ${response.cityName}")
                // re-read to get the auto-generated id
                val fresh = weatherCacheDao.getByLocation(lat, lon)
                Result.success(fresh ?: entity)
            },
            onFailure = { error ->
                // use stale cache as fallback on network error
                val staleCache = weatherCacheDao.getByLocation(lat, lon)
                if (staleCache != null) {
                    AppLogger.w(TAG, "API failed, using stale cache: ${error.message}")
                    Result.success(staleCache)
                } else {
                    Result.failure(error)
                }
            }
        )
    }

    suspend fun getForecast(lat: Double, lon: Double): Result<ForecastResponse> {
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return Result.failure(NoApiKeyException())
        }
        return remoteDataSource.getForecast(lat, lon, apiKey)
    }

    suspend fun clearCache() {
        weatherCacheDao.deleteAll()
        AppLogger.i(TAG, "Weather cache cleared")
    }

    companion object {
        private const val TAG = "WeatherRepository"
    }
}

class NoApiKeyException : Exception("No API key configured. Please set your API key in Settings.")
