package com.example.myapplication.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.airbuddy.MadridPark
import com.example.myapplication.data.airbuddy.MadridParks
import com.example.myapplication.data.airbuddy.TreeScoreManager
import com.example.myapplication.data.airbuddy.TreeStateRepository
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
    val parks: List<MadridPark> = MadridParks.all,
    val visitedParkIds: Set<String> = emptySet(),
    val selectedMarker: CampusMarkerEntity? = null,
    val selectedMarkerAddress: String? = null,
    val selectedPark: MadridPark? = null,
    val parkCheckInResult: ParkCheckInResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val permissionGranted: Boolean = false,
    val usingFallback: Boolean = false
)

data class ParkCheckInResult(
    val parkName: String,
    val xpAwarded: Int
)

class MapViewModel(
    private val locationRepository: LocationRepository,
    private val geocodingRepository: GeocodingRepository,
    private val campusMarkerRepository: CampusMarkerRepository,
    private val treeStateRepository: TreeStateRepository?,
    private val treeScoreManager: TreeScoreManager?,
    private val userId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadCampusMarkers()
        observeTreeState()
    }

    private fun loadCampusMarkers() {
        viewModelScope.launch {
            campusMarkerRepository.allMarkers.collect { markers ->
                _uiState.value = _uiState.value.copy(campusMarkers = markers)
            }
        }
    }

    private fun observeTreeState() {
        if (userId == null || treeStateRepository == null) return
        viewModelScope.launch {
            treeStateRepository.observeTreeState(userId).collect { state ->
                _uiState.value = _uiState.value.copy(
                    visitedParkIds = state.getVisitedParkIdSet()
                )
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        AppLogger.i(TAG, "Permission result: $granted")
        _uiState.value = _uiState.value.copy(permissionGranted = granted)
        if (granted) {
            startLocationUpdates()
        } else {
            AppLogger.w(TAG, "Permission denied, falling back to Madrid")
            _uiState.value = _uiState.value.copy(
                userLatitude = MADRID_LAT,
                userLongitude = MADRID_LNG,
                usingFallback = true,
                isLoading = false
            )
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
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Location updates failed, falling back to Madrid", e)
                _uiState.value = _uiState.value.copy(
                    userLatitude = MADRID_LAT,
                    userLongitude = MADRID_LNG,
                    usingFallback = true,
                    isLoading = false
                )
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

    fun onParkSelected(park: MadridPark) {
        AppLogger.d(TAG, "Park selected: ${park.name}")
        _uiState.value = _uiState.value.copy(selectedPark = park)
    }

    fun dismissParkInfo() {
        _uiState.value = _uiState.value.copy(
            selectedPark = null,
            parkCheckInResult = null
        )
    }

    fun checkInToPark(park: MadridPark) {
        if (userId == null || treeScoreManager == null) {
            AppLogger.w(TAG, "Cannot check in: not signed in")
            return
        }
        viewModelScope.launch {
            val xp = treeScoreManager.onParkVisited(userId, park.id, park.name)
            if (xp > 0) {
                _uiState.value = _uiState.value.copy(
                    parkCheckInResult = ParkCheckInResult(park.name, xp)
                )
            }
        }
    }

    fun clearCheckInResult() {
        _uiState.value = _uiState.value.copy(parkCheckInResult = null)
    }

    class Factory(
        private val locationRepository: LocationRepository,
        private val geocodingRepository: GeocodingRepository,
        private val campusMarkerRepository: CampusMarkerRepository,
        private val treeStateRepository: TreeStateRepository?,
        private val treeScoreManager: TreeScoreManager?,
        private val userId: String?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
                return MapViewModel(
                    locationRepository,
                    geocodingRepository,
                    campusMarkerRepository,
                    treeStateRepository,
                    treeScoreManager,
                    userId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "MapVM"
        private const val MADRID_LAT = 40.4165
        private const val MADRID_LNG = -3.7026
    }
}
