package com.example.myapplication.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.database.entity.WeatherCacheEntity

/**
 * DAO fuer den Weather-Cache.
 * Gecachte Daten werden bei erneutem Abruf ueberschrieben (REPLACE).
 */
@Dao
interface WeatherCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weather: WeatherCacheEntity)

    /** Sucht gecachte Wetterdaten in der Naehe (gerundete Koordinaten) */
    @Query("""
        SELECT * FROM weather_cache
        WHERE ABS(latitude - :lat) < 0.01 AND ABS(longitude - :lon) < 0.01
        ORDER BY fetched_at DESC LIMIT 1
    """)
    suspend fun getByLocation(lat: Double, lon: Double): WeatherCacheEntity?

    @Query("SELECT * FROM weather_cache ORDER BY fetched_at DESC")
    suspend fun getAll(): List<WeatherCacheEntity>

    @Query("DELETE FROM weather_cache")
    suspend fun deleteAll()
}
