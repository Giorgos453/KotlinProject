package com.example.myapplication.ui.csv

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.csv.CsvFileRepository
import com.example.myapplication.data.csv.GpsCoordinate
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

/**
 * UI-State für den CSV-Viewer.
 * Deckt alle Zustände ab: Laden, Daten, Leer, Fehler.
 */
sealed class CsvUiState {
    data object Loading : CsvUiState()
    data class Success(val coordinates: List<GpsCoordinate>) : CsvUiState()
    data object Empty : CsvUiState()
    data class Error(val message: String) : CsvUiState()
}

/**
 * ViewModel für den CSV-Viewer.
 * Überlebt Konfigurationsänderungen (Rotation) – Datei wird nicht erneut gelesen.
 * Datenzugriff läuft über das CsvFileRepository (nicht direkt im ViewModel).
 */
class CsvViewModel(
    private val repository: CsvFileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CsvUiState>(CsvUiState.Loading)
    val uiState: StateFlow<CsvUiState> = _uiState.asStateFlow()

    init {
        loadCsvData()
    }

    /** Lädt die CSV-Daten. Kann auch für Pull-to-Refresh aufgerufen werden. */
    fun loadCsvData() {
        viewModelScope.launch {
            _uiState.value = CsvUiState.Loading
            try {
                val coordinates = repository.readGpsCoordinates()
                _uiState.value = if (coordinates.isEmpty()) {
                    CsvUiState.Empty
                } else {
                    CsvUiState.Success(coordinates)
                }
            } catch (e: FileNotFoundException) {
                AppLogger.i(TAG, "No CSV file found yet")
                _uiState.value = CsvUiState.Empty
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error loading CSV", e)
                _uiState.value = CsvUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Factory für die Erstellung des ViewModels mit Repository-Dependency.
     * Wird von der Activity verwendet, um das ViewModel korrekt zu instanziieren.
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CsvViewModel::class.java)) {
                val repository = CsvFileRepository(context.applicationContext)
                return CsvViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        private const val TAG = "CsvViewModel"
    }
}
