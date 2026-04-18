package com.example.myapplication.data.csv

import android.content.Context
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class CsvFileRepository(private val context: Context) {

    suspend fun readGpsCoordinates(): List<GpsCoordinate> = withContext(Dispatchers.IO) {
        val csvFile = File(context.filesDir, CSV_FILE_NAME)

        if (!csvFile.exists()) {
            throw FileNotFoundException("CSV file not found: $CSV_FILE_NAME")
        }

        // useLines auto-closes the reader to avoid resource leaks
        csvFile.bufferedReader().useLines { lines ->
            lines
                .drop(1) // skip header row
                .filter { it.isNotBlank() }
                .mapNotNull { line -> parseLine(line) }
                .toList()
        }.also {
            AppLogger.i(TAG, "Read ${it.size} GPS entries from CSV")
        }
    }

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
        const val CSV_FILE_NAME = "gps_coordinates.csv"
        private const val EXPECTED_COLUMN_COUNT = 4
    }
}
