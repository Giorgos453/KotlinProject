package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.app.Activity
import com.example.myapplication.ui.location.LocationViewModel
import com.example.myapplication.util.AppLogger
import java.io.File

/**
 * SecondActivity: Liest den Inhalt der CSV-Datei (gps_coordinates.csv)
 * und zeigt ihn in einer TextView an.
 * Nutzt useLines für sicheres Ressourcenmanagement.
 */
class SecondActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        AppLogger.i(TAG, "onCreate")

        val tvCsvContent = findViewById<TextView>(R.id.tvCsvContent)
        val btnBack = findViewById<Button>(R.id.btnBack)

        // CSV-Datei auslesen und in der TextView anzeigen
        loadCsvContent(tvCsvContent)

        // Zurück-Button: Activity schließen
        btnBack.setOnClickListener {
            AppLogger.d(TAG, "Back button pressed")
            finish()
        }
    }

    /**
     * Liest die CSV-Datei zeilenweise mit useLines (sicheres Ressourcenmanagement)
     * und zeigt den Inhalt in der übergebenen TextView an.
     */
    private fun loadCsvContent(textView: TextView) {
        try {
            val csvFile = File(filesDir, LocationViewModel.CSV_FILE_NAME)
            if (csvFile.exists()) {
                // useLines schließt den Reader automatisch nach dem Lesen
                val content = csvFile.bufferedReader().useLines { lines ->
                    lines.joinToString("\n")
                }
                textView.text = content
                AppLogger.i(TAG, "CSV loaded successfully")
            } else {
                textView.text = getString(R.string.no_data_available)
                AppLogger.i(TAG, "CSV file does not exist yet")
            }
        } catch (e: Exception) {
            textView.text = getString(R.string.error_reading_csv, e.message)
            AppLogger.e(TAG, "Error reading CSV file", e)
        }
    }

    companion object {
        private const val TAG = "SecondActivity"
    }
}
