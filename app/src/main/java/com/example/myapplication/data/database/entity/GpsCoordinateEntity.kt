package com.example.myapplication.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for GPS coordinates.
 * Replaces the previous CSV file as persistent data source.
 * Indexed on timestamp for fast chronological queries.
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
