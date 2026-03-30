package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.util.AppLogger
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import es.upm.btb.helloworldkt.persistence.room.AppDatabase
import es.upm.btb.helloworldkt.persistence.room.LocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ThirdActivity: Detail-Screen fuer einen einzelnen GPS-Eintrag.
 * Zeigt Details an und bietet Update + Delete mit Bestaetigungsdialogen.
 */
class ThirdActivity : AppCompatActivity() {

    private var coordinateId: Int = 0
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        AppLogger.i(TAG, "onCreate")

        setupToolbar()

        val coordinate = readIntentData()
        if (coordinate != null) {
            coordinateId = coordinate.id
            displayCoordinate(coordinate)
            setupUpdateButton(coordinate)
            setupDeleteButton(coordinate)
        } else {
            showError()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            AppLogger.d(TAG, "Back navigation pressed")
            finish()
        }
    }

    private fun readIntentData(): LocationEntity? {
        val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, -1L)
        if (timestamp == -1L) return null
        return LocationEntity(
            id = intent.getIntExtra(EXTRA_ID, 0),
            latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0),
            longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0),
            altitude = intent.getDoubleExtra(EXTRA_ALTITUDE, 0.0),
            timestamp = timestamp
        )
    }

    private fun displayCoordinate(coordinate: LocationEntity) {
        val formattedTime = dateFormat.format(Date(coordinate.timestamp))
        AppLogger.d(TAG, "Displaying coordinate: $formattedTime")

        findViewById<TextView>(R.id.tvDetailTimestamp).text = formattedTime
        findViewById<TextView>(R.id.tvDetailLatitude).text =
            "%.6f\u00B0".format(coordinate.latitude)
        findViewById<TextView>(R.id.tvDetailLongitude).text =
            "%.6f\u00B0".format(coordinate.longitude)
        findViewById<TextView>(R.id.tvDetailAltitude).text =
            "%.1f m".format(coordinate.altitude)
    }

    /** Update-Button: Zeigt Bestaetigungsdialog mit den aktuellen Werten vor dem Speichern */
    private fun setupUpdateButton(coordinate: LocationEntity) {
        val btnUpdate = findViewById<MaterialButton>(R.id.btnUpdate)
        btnUpdate.visibility = View.VISIBLE
        btnUpdate.setOnClickListener {
            val formattedTime = dateFormat.format(Date(coordinate.timestamp))
            val message = getString(
                R.string.detail_update_confirm_message,
                formattedTime,
                coordinate.latitude,
                coordinate.longitude,
                coordinate.altitude
            )
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.detail_update_confirm_title)
                .setMessage(message)
                .setPositiveButton(R.string.detail_update) { _, _ ->
                    updateAndRefresh(coordinate)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    /** Delete-Button: Zeigt Bestaetigungsdialog mit Koordinaten-Details */
    private fun setupDeleteButton(coordinate: LocationEntity) {
        val btnDelete = findViewById<MaterialButton>(R.id.btnDelete)
        btnDelete.visibility = View.VISIBLE
        btnDelete.setOnClickListener {
            val formattedTime = dateFormat.format(Date(coordinate.timestamp))
            val message = getString(
                R.string.detail_delete_confirm_details,
                formattedTime,
                coordinate.latitude,
                coordinate.longitude,
                coordinate.altitude
            )
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.detail_delete_confirm_title)
                .setMessage(message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    deleteAndFinish(coordinate)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    /** Aktualisiert den Timestamp des Eintrags und refresht die Anzeige */
    private fun updateAndRefresh(coordinate: LocationEntity) {
        lifecycleScope.launch {
            try {
                val dao = AppDatabase.getInstance(applicationContext).locationDao()
                val updated = coordinate.copy(timestamp = System.currentTimeMillis())
                withContext(Dispatchers.IO) {
                    dao.update(updated)
                }
                AppLogger.i(TAG, "GPS entry updated: ${coordinate.id}")
                displayCoordinate(updated)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error updating GPS entry", e)
            }
        }
    }

    private fun deleteAndFinish(coordinate: LocationEntity) {
        lifecycleScope.launch {
            try {
                val dao = AppDatabase.getInstance(applicationContext).locationDao()
                withContext(Dispatchers.IO) {
                    dao.deleteById(coordinate.id)
                }
                AppLogger.i(TAG, "GPS entry deleted: ${coordinate.id}")
                finish()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error deleting GPS entry", e)
            }
        }
    }

    private fun showError() {
        AppLogger.e(TAG, "No coordinate data in intent")
        findViewById<TextView>(R.id.tvDetailError).visibility = View.VISIBLE
    }

    companion object {
        private const val TAG = "ThirdActivity"

        private const val EXTRA_ID = "extra_id"
        private const val EXTRA_TIMESTAMP = "extra_timestamp"
        private const val EXTRA_LATITUDE = "extra_latitude"
        private const val EXTRA_LONGITUDE = "extra_longitude"
        private const val EXTRA_ALTITUDE = "extra_altitude"

        fun newIntent(context: Context, coordinate: LocationEntity): Intent {
            return Intent(context, ThirdActivity::class.java).apply {
                putExtra(EXTRA_ID, coordinate.id)
                putExtra(EXTRA_TIMESTAMP, coordinate.timestamp)
                putExtra(EXTRA_LATITUDE, coordinate.latitude)
                putExtra(EXTRA_LONGITUDE, coordinate.longitude)
                putExtra(EXTRA_ALTITUDE, coordinate.altitude)
            }
        }
    }
}
