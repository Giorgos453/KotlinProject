package com.example.myapplication.ui.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.location.LocationRepository
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissionGranted: Boolean = false,
    // Switch-Status: Aufzeichnung in CSV-Datei aktiviert/deaktiviert
    val isRecordingEnabled: Boolean = false
)

class LocationViewModel(
    private val locationRepository: LocationRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    // Job für kontinuierliche Location-Updates (wird beim Ausschalten des Switch gecancelt)
    private var locationUpdatesJob: Job? = null

    fun onPermissionResult(granted: Boolean) {
        AppLogger.i(TAG, "Permission result: $granted")
        _uiState.value = _uiState.value.copy(permissionGranted = granted)
        if (granted) {
            fetchLocation()
        } else {
            _uiState.value = _uiState.value.copy(error = "Location permission denied")
        }
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            locationRepository.getCurrentLocation()
                .onSuccess { location ->
                    AppLogger.d(TAG, "Location fetched: ${location.latitude}, ${location.longitude}, alt=${location.altitude}")
                    _uiState.value = _uiState.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        altitude = location.altitude,
                        isLoading = false
                    )
                    // CSV-Aufzeichnung, wenn der Switch aktiviert ist
                    if (_uiState.value.isRecordingEnabled) {
                        writeLocationToCsv(location.latitude, location.longitude, location.altitude)
                    }
                }
                .onFailure { e ->
                    AppLogger.e(TAG, "Location fetch failed", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
        }
    }

    /**
     * Schaltet die Location-Aufzeichnung ein oder aus.
     * Bei Aktivierung: Startet kontinuierliche GPS-Updates (alle 5s / 5m).
     * Bei Deaktivierung: Stoppt die Updates.
     */
    fun toggleRecording(enabled: Boolean) {
        AppLogger.i(TAG, "Recording toggled: $enabled")
        _uiState.value = _uiState.value.copy(isRecordingEnabled = enabled)

        if (enabled) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }

    /** Startet kontinuierliche Location-Updates über den LocationRepository Flow */
    private fun startLocationUpdates() {
        // Falls schon aktiv, nicht nochmal starten
        if (locationUpdatesJob?.isActive == true) return

        locationUpdatesJob = viewModelScope.launch {
            AppLogger.i(TAG, "Starting continuous location updates")
            locationRepository.getLocationUpdates().collect { location ->
                AppLogger.d(TAG, "Location update: ${location.latitude}, ${location.longitude}, alt=${location.altitude}")
                _uiState.value = _uiState.value.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    altitude = location.altitude,
                    isLoading = false
                )
                // CSV-Aufzeichnung bei jedem Update
                writeLocationToCsv(location.latitude, location.longitude, location.altitude)
            }
        }
    }

    /** Stoppt die kontinuierlichen Location-Updates */
    private fun stopLocationUpdates() {
        AppLogger.i(TAG, "Stopping continuous location updates")
        locationUpdatesJob?.cancel()
        locationUpdatesJob = null
    }

    /**
     * Schreibt einen GPS-Datensatz in die CSV-Datei gps_coordinates.csv.
     * Format: Zeitstempel,Breitengrad,Längengrad,Höhe (jeweils 4 Dezimalstellen)
     */
    private fun writeLocationToCsv(latitude: Double, longitude: Double, altitude: Double) {
        try {
            val csvFile = File(appContext.filesDir, CSV_FILE_NAME)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            // Koordinaten mit 4 Dezimalstellen formatieren
            val line = "$timestamp,%.4f,%.4f,%.4f".format(latitude, longitude, altitude)

            // CSV-Header schreiben, falls Datei noch nicht existiert
            if (!csvFile.exists()) {
                csvFile.writeText("timestamp,latitude,longitude,altitude\n")
            }
            csvFile.appendText("$line\n")

            AppLogger.i(TAG, "CSV written: $line")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to write CSV", e)
        }
    }

    class Factory(
        private val locationRepository: LocationRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
                return LocationViewModel(locationRepository, appContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "LocationVM"
        const val CSV_FILE_NAME = "gps_coordinates.csv"
    }
}
