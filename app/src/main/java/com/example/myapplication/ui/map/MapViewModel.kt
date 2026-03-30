package com.example.myapplication.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.entity.CampusMarkerEntity
import com.example.myapplication.data.database.repository.CampusMarkerRepository
import com.example.myapplication.data.geocoding.GeocodingRepository
import com.example.myapplication.data.location.LocationRepository
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapUiState(
    val userLatitude: Double? = null,
    val userLongitude: Double? = null,
    val campusMarkers: List<CampusMarkerEntity> = emptyList(),
    val selectedMarker: CampusMarkerEntity? = null,
    val userAddress: String? = null,
    val selectedMarkerAddress: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissionGranted: Boolean = false
)

/**
 * ViewModel fuer Map-Screen.
 * Laedt Campus-Marker jetzt aus der Room-Datenbank statt aus statischer Liste.
 */
class MapViewModel(
    private val locationRepository: LocationRepository,
    private val geocodingRepository: GeocodingRepository,
    private val campusMarkerRepository: CampusMarkerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // Campus-Marker aus Room-Datenbank laden (Flow-basiert)
        loadCampusMarkers()
    }

    /** Beobachtet Campus-Marker als Flow – UI aktualisiert sich automatisch */
    private fun loadCampusMarkers() {
        viewModelScope.launch {
            campusMarkerRepository.allMarkers.collect { markers ->
                _uiState.value = _uiState.value.copy(campusMarkers = markers)
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        AppLogger.i(TAG, "Permission result: $granted")
        _uiState.value = _uiState.value.copy(permissionGranted = granted)
        if (granted) {
            startLocationUpdates()
        } else {
            _uiState.value = _uiState.value.copy(error = "Location permission denied")
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                locationRepository.getLocationUpdates().collect { location ->
                    AppLogger.d(TAG, "Location update: ${location.latitude}, ${location.longitude}")
                    _uiState.value = _uiState.value.copy(
                        userLatitude = location.latitude,
                        userLongitude = location.longitude,
                        isLoading = false
                    )
                    geocodeUserLocation(location.latitude, location.longitude)
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Location updates failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun geocodeUserLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            geocodingRepository.reverseGeocode(latitude, longitude)
                .onSuccess { address ->
                    _uiState.value = _uiState.value.copy(userAddress = address)
                }
                .onFailure { e ->
                    AppLogger.e(TAG, "User geocoding failed", e)
                }
        }
    }

    fun onMarkerSelected(marker: CampusMarkerEntity) {
        AppLogger.d(TAG, "Marker selected: ${marker.title}")
        _uiState.value = _uiState.value.copy(
            selectedMarker = marker,
            selectedMarkerAddress = null
        )
        viewModelScope.launch {
            geocodingRepository.reverseGeocode(marker.latitude, marker.longitude)
                .onSuccess { address ->
                    _uiState.value = _uiState.value.copy(selectedMarkerAddress = address)
                }
                .onFailure { e ->
                    AppLogger.e(TAG, "Marker geocoding failed", e)
                }
        }
    }

    fun dismissMarkerInfo() {
        _uiState.value = _uiState.value.copy(
            selectedMarker = null,
            selectedMarkerAddress = null
        )
    }

    class Factory(
        private val locationRepository: LocationRepository,
        private val geocodingRepository: GeocodingRepository,
        private val campusMarkerRepository: CampusMarkerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                return MapViewModel(locationRepository, geocodingRepository, campusMarkerRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "MapVM"
    }
}
