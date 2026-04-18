package com.example.myapplication.data.csv

/**
 * Data model for a GPS entry from the CSV file.
 * Format: timestamp,latitude,longitude,altitude
 * Example: 2026-03-26 14:30:00,48.2082,16.3738,171.0000
 */
data class GpsCoordinate(
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)
