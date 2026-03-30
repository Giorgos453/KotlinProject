package com.example.myapplication.data.network

import com.example.myapplication.data.network.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit-Interface fuer die OpenWeatherMap API.
 * Alle Endpunkte als suspend fun (Coroutine-kompatibel).
 * API-Key wird als Parameter uebergeben – nicht hardcoded.
 */
interface WeatherApiService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): WeatherResponse
}
