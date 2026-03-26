package com.example.myapplication.data.csv

import android.content.Context
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

/**
 * Repository für den Zugriff auf die GPS-CSV-Datei.
 * Kapselt alle Datei-Leseoperationen – kein direkter File-Zugriff in Activity/ViewModel.
 * Alle IO-Operationen laufen auf Dispatchers.IO (nicht auf dem Main Thread).
 */
class CsvFileRepository(private val context: Context) {

    /**
     * Liest die CSV-Datei und gibt eine Liste von GpsCoordinate zurück.
     * Wirft Exceptions bei Fehler (FileNotFound, Parse-Fehler etc.).
     *
     * CSV-Format: timestamp,latitude,longitude,altitude
     * Erste Zeile ist der Header und wird übersprungen.
     */
    suspend fun readGpsCoordinates(): List<GpsCoordinate> = withContext(Dispatchers.IO) {
        val csvFile = File(context.filesDir, CSV_FILE_NAME)

        if (!csvFile.exists()) {
            throw FileNotFoundException("CSV file not found: $CSV_FILE_NAME")
        }

        // useLines schließt den Reader automatisch (kein Ressourcenleck)
        csvFile.bufferedReader().useLines { lines ->
            lines
                .drop(1) // Header-Zeile überspringen
                .filter { it.isNotBlank() }
                .mapNotNull { line -> parseLine(line) }
                .toList()
        }.also {
            AppLogger.i(TAG, "Read ${it.size} GPS entries from CSV")
        }
    }

    /**
     * Parst eine einzelne CSV-Zeile in ein GpsCoordinate-Objekt.
     * Gibt null zurück, wenn die Zeile nicht geparst werden kann (statt Crash).
     */
    private fun parseLine(line: String): GpsCoordinate? {
        return try {
            val parts = line.split(",")
            if (parts.size < EXPECTED_COLUMN_COUNT) {
                AppLogger.w(TAG, "Skipping malformed line: $line")
                return null
            }
            GpsCoordinate(
                timestamp = parts[0].trim(),
                latitude = parts[1].trim().toDouble(),
                longitude = parts[2].trim().toDouble(),
                altitude = parts[3].trim().toDouble()
            )
        } catch (e: NumberFormatException) {
            AppLogger.w(TAG, "Skipping line with invalid numbers: $line")
            null
        }
    }

    companion object {
        private const val TAG = "CsvFileRepository"
        /** Dateiname der GPS-CSV – identisch mit LocationViewModel.CSV_FILE_NAME */
        const val CSV_FILE_NAME = "gps_coordinates.csv"
        /** Erwartete Anzahl Spalten: timestamp, lat, lon, altitude */
        private const val EXPECTED_COLUMN_COUNT = 4
    }
}
