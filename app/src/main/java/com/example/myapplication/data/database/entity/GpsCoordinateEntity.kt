package com.example.myapplication.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room-Entity fuer GPS-Koordinaten.
 * Ersetzt die bisherige CSV-Datei als persistente Datenquelle.
 * Index auf timestamp fuer schnelle chronologische Abfragen.
 */
@Entity(
    tableName = "gps_coordinates",
    indices = [Index(value = ["timestamp"])]
)
data class GpsCoordinateEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "timestamp") val timestamp: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "altitude") val altitude: Double,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
