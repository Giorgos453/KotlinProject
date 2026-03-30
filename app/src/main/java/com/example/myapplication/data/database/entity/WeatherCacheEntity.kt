package com.example.myapplication.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room-Entity fuer gecachte Wetterdaten.
 * Verknuepft Wetterdaten mit GPS-Koordinaten (abgerundet auf 2 Dezimalstellen).
 * Cache wird mit REPLACE-Strategie aktualisiert bei erneutem Abruf.
 */
@Entity(
    tableName = "weather_cache",
    indices = [Index(value = ["latitude", "longitude"])]
)
data class WeatherCacheEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "city_name") val cityName: String,
    @ColumnInfo(name = "temperature") val temperature: Double,
    @ColumnInfo(name = "feels_like") val feelsLike: Double,
    @ColumnInfo(name = "humidity") val humidity: Int,
    @ColumnInfo(name = "pressure") val pressure: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "icon_code") val iconCode: String,
    @ColumnInfo(name = "wind_speed") val windSpeed: Double,
    @ColumnInfo(name = "fetched_at") val fetchedAt: Long = System.currentTimeMillis()
) {
    /** URL fuer das Wetter-Icon */
    val iconUrl: String get() = "https://openweathermap.org/img/wn/${iconCode}@2x.png"

    /** Cache ist aelter als 10 Minuten */
    val isExpired: Boolean get() = System.currentTimeMillis() - fetchedAt > CACHE_DURATION_MS

    companion object {
        private const val CACHE_DURATION_MS = 10 * 60 * 1000L // 10 Minuten
    }
}
