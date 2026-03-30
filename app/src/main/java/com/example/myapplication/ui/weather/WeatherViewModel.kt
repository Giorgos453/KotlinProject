package com.example.myapplication.ui.weather

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.entity.WeatherCacheEntity
import com.example.myapplication.data.network.NoApiKeyException
import com.example.myapplication.data.network.WeatherRepository
import com.example.myapplication.util.NetworkUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel fuer den Weather-Screen.
 * Verwaltet UI-State als StateFlow mit drei Zustaenden: Loading, Success, Error.
 */
class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /**
     * Laedt Wetterdaten fuer die angegebenen Koordinaten.
     * Prueft vorher Netzwerkverfuegbarkeit.
     */
    fun loadWeather(lat: Double, lon: Double, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            // Netzwerk-Check (nur Warnung, Cache kann trotzdem funktionieren)
            val hasNetwork = NetworkUtil.isNetworkAvailable(appContext)

            val result = weatherRepository.getWeather(lat, lon, forceRefresh)
            _uiState.value = result.fold(
                onSuccess = { weather ->
                    WeatherUiState.Success(weather, fromCache = !hasNetwork)
                },
                onFailure = { error ->
                    when (error) {
                        is NoApiKeyException -> WeatherUiState.NoApiKey
                        else -> WeatherUiState.Error(
                            error.message ?: "Unknown error occurred"
                        )
                    }
                }
            )
        }
    }

    /** Erzwingt einen frischen API-Aufruf */
    fun refresh(lat: Double, lon: Double) {
        loadWeather(lat, lon, forceRefresh = true)
    }

    /** Factory fuer WeatherViewModel mit Repository und Context */
    class Factory(
        private val weatherRepository: WeatherRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
                return WeatherViewModel(weatherRepository, appContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/** Sealed class fuer alle moeglichen UI-Zustaende des Weather-Screens */
sealed class WeatherUiState {
    /** Initialzustand – noch keine Daten geladen */
    data object Initial : WeatherUiState()

    /** Daten werden geladen */
    data object Loading : WeatherUiState()

    /** Wetterdaten erfolgreich geladen */
    data class Success(
        val weather: WeatherCacheEntity,
        val fromCache: Boolean = false
    ) : WeatherUiState()

    /** Fehler beim Laden */
    data class Error(val message: String) : WeatherUiState()

    /** Kein API-Key konfiguriert */
    data object NoApiKey : WeatherUiState()
}
