package com.example.myapplication.data.csv

/**
 * Datenmodell für einen GPS-Eintrag aus der CSV-Datei.
 * Format: timestamp,latitude,longitude,altitude
 * Beispiel: 2026-03-26 14:30:00,48.2082,16.3738,171.0000
 */
data class GpsCoordinate(
    val timestamp: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)
