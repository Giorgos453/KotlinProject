package com.example.myapplication.ui.csv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.repository.GpsCoordinateRepository
import com.example.myapplication.util.AppLogger
import es.upm.btb.helloworldkt.persistence.room.LocationEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CsvUiState {
    data object Loading : CsvUiState()
    data class Success(val coordinates: List<LocationEntity>) : CsvUiState()
    data object Empty : CsvUiState()
    data class Error(val message: String) : CsvUiState()
}

class CsvViewModel(
    private val repository: GpsCoordinateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CsvUiState>(CsvUiState.Loading)
    val uiState: StateFlow<CsvUiState> = _uiState.asStateFlow()

    init {
        loadGpsData()
    }

    private fun loadGpsData() {
        viewModelScope.launch {
            try {
                repository.allCoordinates.collect { coordinates ->
                    _uiState.value = if (coordinates.isEmpty()) {
                        CsvUiState.Empty
                    } else {
                        CsvUiState.Success(coordinates)
                    }
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error loading GPS data from database", e)
                _uiState.value = CsvUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun insertCoordinate(coordinate: LocationEntity) {
        viewModelScope.launch {
            try {
                repository.insert(coordinate)
                AppLogger.i(TAG, "GPS entry re-inserted: ${coordinate.id}")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error re-inserting GPS entry", e)
            }
        }
    }

    fun deleteCoordinate(coordinate: LocationEntity) {
        viewModelScope.launch {
            try {
                repository.delete(coordinate)
                AppLogger.i(TAG, "GPS entry deleted: ${coordinate.id}")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error deleting GPS entry", e)
            }
        }
    }

    fun deleteAllCoordinates() {
        viewModelScope.launch {
            try {
                repository.deleteAll()
                AppLogger.i(TAG, "All GPS entries deleted")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error deleting all GPS entries", e)
            }
        }
    }

    class Factory(
        private val repository: GpsCoordinateRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CsvViewModel::class.java)) {
                return CsvViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        private const val TAG = "CsvViewModel"
    }
}
