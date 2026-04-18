package com.example.myapplication.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.repository.GpsCoordinateRepository
import com.example.myapplication.data.location.LocationRepository
import com.example.myapplication.util.AppLogger
import es.upm.btb.helloworldkt.persistence.room.LocationEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocationUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissionGranted: Boolean = false,
    // switch state: recording to database enabled/disabled
    val isRecordingEnabled: Boolean = false
)

/**
 * ViewModel for the Location screen.
 * Writes GPS data into the Room database.
 */
class LocationViewModel(
    private val locationRepository: LocationRepository,
    private val gpsRepository: GpsCoordinateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    // job for continuous location updates
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
                    if (_uiState.value.isRecordingEnabled) {
                        saveLocationToDatabase(location.latitude, location.longitude, location.altitude)
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

    fun toggleRecording(enabled: Boolean) {
        AppLogger.i(TAG, "Recording toggled: $enabled")
        _uiState.value = _uiState.value.copy(isRecordingEnabled = enabled)

        if (enabled) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
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
                // persist GPS data into Room database
                saveLocationToDatabase(location.latitude, location.longitude, location.altitude)
            }
        }
    }

    private fun stopLocationUpdates() {
        AppLogger.i(TAG, "Stopping continuous location updates")
        locationUpdatesJob?.cancel()
        locationUpdatesJob = null
    }

    /**
     * Persists a single GPS record into the Room database.
     * Timestamp stored as Long (System.currentTimeMillis()).
     */
    private fun saveLocationToDatabase(latitude: Double, longitude: Double, altitude: Double) {
        viewModelScope.launch {
            try {
                val entity = LocationEntity(
                    latitude = "%.4f".format(latitude).toDouble(),
                    longitude = "%.4f".format(longitude).toDouble(),
                    altitude = "%.4f".format(altitude).toDouble(),
                    timestamp = System.currentTimeMillis()
                )
                gpsRepository.insert(entity)
                AppLogger.i(TAG, "GPS saved to database: ${entity.timestamp}")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to save GPS to database", e)
            }
        }
    }

    class Factory(
        private val locationRepository: LocationRepository,
        private val gpsRepository: GpsCoordinateRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
                return LocationViewModel(locationRepository, gpsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "LocationVM"
    }
}
