package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.data.csv.GpsCoordinate
import com.example.myapplication.util.AppLogger
import com.google.android.material.appbar.MaterialToolbar

/**
 * ThirdActivity: Detail-Screen für einen einzelnen GPS-Eintrag.
 *
 * Refactored: Ursprünglich war dies eine Demo-Activity für finish().
 * Jetzt zeigt sie die Details eines angeklickten GPS-Listeneintrags an.
 * Die Daten werden als einfache Typen per Intent.putExtra() übergeben –
 * Keys sind als Konstanten im companion object definiert.
 */
class ThirdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        AppLogger.i(TAG, "onCreate")

        setupToolbar()

        // Intent-Daten sicher auslesen
        val coordinate = readIntentData()
        if (coordinate != null) {
            displayCoordinate(coordinate)
        } else {
            showError()
        }
    }

    /** Toolbar mit Zurück-Navigation konfigurieren */
    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            AppLogger.d(TAG, "Back navigation pressed")
            finish()
        }
    }

    /**
     * Extrahiert die GPS-Daten aus dem Intent.
     * Gibt null zurück, falls der Timestamp fehlt (= kein gültiger Eintrag).
     * Alle Werte werden mit sicherem Fallback ausgelesen.
     */
    private fun readIntentData(): GpsCoordinate? {
        val timestamp = intent.getStringExtra(EXTRA_TIMESTAMP) ?: return null
        return GpsCoordinate(
            timestamp = timestamp,
            latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0),
            longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0),
            altitude = intent.getDoubleExtra(EXTRA_ALTITUDE, 0.0)
        )
    }

    /** Zeigt alle Felder des GPS-Eintrags in den Detail-Cards an */
    private fun displayCoordinate(coordinate: GpsCoordinate) {
        AppLogger.d(TAG, "Displaying coordinate: ${coordinate.timestamp}")

        findViewById<TextView>(R.id.tvDetailTimestamp).text = coordinate.timestamp
        findViewById<TextView>(R.id.tvDetailLatitude).text =
            "%.6f°".format(coordinate.latitude)
        findViewById<TextView>(R.id.tvDetailLongitude).text =
            "%.6f°".format(coordinate.longitude)
        findViewById<TextView>(R.id.tvDetailAltitude).text =
            "%.1f m".format(coordinate.altitude)
    }

    /** Zeigt Fehlermeldung an, falls Intent-Daten fehlen oder ungültig sind */
    private fun showError() {
        AppLogger.e(TAG, "No coordinate data in intent")
        findViewById<TextView>(R.id.tvDetailError).visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "ThirdActivity"

        // Intent-Extra Keys als Konstanten – kein Magic String außerhalb dieser Klasse
        private const val EXTRA_TIMESTAMP = "extra_timestamp"
        private const val EXTRA_LATITUDE = "extra_latitude"
        private const val EXTRA_LONGITUDE = "extra_longitude"
        private const val EXTRA_ALTITUDE = "extra_altitude"

        /**
         * Factory-Methode für typsicheren Intent-Aufruf.
         * Kein direktes Intent-Bauen außerhalb dieser Activity nötig.
         */
        fun newIntent(context: Context, coordinate: GpsCoordinate): Intent {
            return Intent(context, ThirdActivity::class.java).apply {
                putExtra(EXTRA_TIMESTAMP, coordinate.timestamp)
                putExtra(EXTRA_LATITUDE, coordinate.latitude)
                putExtra(EXTRA_LONGITUDE, coordinate.longitude)
                putExtra(EXTRA_ALTITUDE, coordinate.altitude)
            }
        }
    }
}
