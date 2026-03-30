package com.example.myapplication.data.network

import com.example.myapplication.data.database.dao.WeatherCacheDao
import com.example.myapplication.data.database.entity.WeatherCacheEntity
import com.example.myapplication.data.security.ApiKeyManager
import com.example.myapplication.util.AppLogger

/**
 * Repository fuer Wetterdaten – entscheidet ob Cache oder API genutzt wird.
 *
 * Strategie:
 * 1. Cache pruefen (nach Koordinaten, < 10 Min alt)
 * 2. Bei Cache-Hit: sofort zurueckgeben
 * 3. Bei Cache-Miss oder abgelaufen: API aufrufen, Cache aktualisieren
 */
class WeatherRepository(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val weatherCacheDao: WeatherCacheDao,
    private val apiKeyManager: ApiKeyManager
) {

    /**
     * Laedt Wetterdaten – zuerst aus Cache, dann von API falls noetig.
     * @param forceRefresh Erzwingt API-Aufruf auch bei gueltigem Cache
     */
    suspend fun getWeather(
        lat: Double,
        lon: Double,
        forceRefresh: Boolean = false
    ): Result<WeatherCacheEntity> {
        // API-Key pruefen
        val apiKey = apiKeyManager.getApiKey()
        if (apiKey.isNullOrBlank()) {
            return Result.failure(NoApiKeyException())
        }

        // Cache pruefen (wenn kein forceRefresh)
        if (!forceRefresh) {
            val cached = weatherCacheDao.getByLocation(lat, lon)
            if (cached != null && !cached.isExpired) {
                AppLogger.d(TAG, "Cache hit for ($lat, $lon)")
                return Result.success(cached)
            }
        }

        // API aufrufen
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
                // Frisch aus Cache lesen (mit generierter ID)
                val fresh = weatherCacheDao.getByLocation(lat, lon)
                Result.success(fresh ?: entity)
            },
            onFailure = { error ->
                // Bei Netzwerkfehler: abgelaufenen Cache als Fallback nutzen
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

    /** Loescht den gesamten Weather-Cache */
    suspend fun clearCache() {
        weatherCacheDao.deleteAll()
        AppLogger.i(TAG, "Weather cache cleared")
    }

    companion object {
        private const val TAG = "WeatherRepository"
    }
}

/** Wird geworfen wenn kein API-Key konfiguriert ist */
class NoApiKeyException : Exception("No API key configured. Please set your API key in Settings.")
