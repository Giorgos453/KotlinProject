package com.example.myapplication.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * API-Response-Modelle fuer OpenWeatherMap /data/2.5/weather.
 * SerializedName mappt JSON-Keys auf Kotlin-Properties.
 */
data class WeatherResponse(
    @SerializedName("name") val cityName: String,
    @SerializedName("main") val main: MainData,
    @SerializedName("weather") val weather: List<WeatherItem>,
    @SerializedName("wind") val wind: WindData?,
    @SerializedName("coord") val coord: CoordData?
)

data class MainData(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("pressure") val pressure: Int
)

data class WeatherItem(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
) {
    /** URL fuer das Wetter-Icon von OpenWeatherMap */
    val iconUrl: String get() = "https://openweathermap.org/img/wn/${icon}@2x.png"
}

data class WindData(
    @SerializedName("speed") val speed: Double
)

data class CoordData(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double
)
