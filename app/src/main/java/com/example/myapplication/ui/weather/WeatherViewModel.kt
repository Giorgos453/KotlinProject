package com.example.myapplication.ui.weather

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.entity.WeatherCacheEntity
import com.example.myapplication.data.network.NoApiKeyException
import com.example.myapplication.data.network.WeatherRepository
import com.example.myapplication.data.network.model.ForecastItem
import com.example.myapplication.util.AppLogger
import com.example.myapplication.util.NetworkUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _forecast = MutableStateFlow<List<ForecastItem>>(emptyList())
    val forecast: StateFlow<List<ForecastItem>> = _forecast.asStateFlow()

    fun loadWeather(lat: Double, lon: Double, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

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

            loadForecast(lat, lon)
        }
    }

    private fun loadForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            val result = weatherRepository.getForecast(lat, lon)
            result.fold(
                onSuccess = { response ->
                    _forecast.value = response.list
                    AppLogger.i(TAG, "Forecast loaded: ${response.list.size} items")
                },
                onFailure = { error ->
                    AppLogger.w(TAG, "Forecast fetch failed: ${error.message}")
                }
            )
        }
    }

    fun refresh(lat: Double, lon: Double) {
        loadWeather(lat, lon, forceRefresh = true)
    }

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

    companion object {
        private const val TAG = "WeatherViewModel"
    }
}

sealed class WeatherUiState {
    data object Initial : WeatherUiState()
    data object Loading : WeatherUiState()
    data class Success(
        val weather: WeatherCacheEntity,
        val fromCache: Boolean = false
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
    data object NoApiKey : WeatherUiState()
}

data class DailyForecast(
    val dayLabel: String,
    val dateMillis: Long,
    val iconEmoji: String,
    val condition: String,
    val maxTemp: Double,
    val minTemp: Double,
    val items: List<ForecastItem>
)

fun weatherEmoji(main: String?): String = when (main?.lowercase()) {
    "clear" -> "\u2600\uFE0F"
    "clouds" -> "\u2601\uFE0F"
    "rain" -> "\uD83C\uDF27\uFE0F"
    "drizzle" -> "\uD83C\uDF26\uFE0F"
    "snow" -> "\u2744\uFE0F"
    "thunderstorm" -> "\u26C8\uFE0F"
    "mist", "fog", "haze", "smoke", "dust", "sand", "squall" -> "\uD83C\uDF2B\uFE0F"
    else -> "\uD83C\uDF24\uFE0F"
}

fun groupForecastByDay(items: List<ForecastItem>): List<DailyForecast> {
    if (items.isEmpty()) return emptyList()
    val tz = TimeZone.getDefault()
    val cal = Calendar.getInstance(tz)
    val today = Calendar.getInstance(tz).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val dayNames = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    return items.groupBy { item ->
        cal.time = Date(item.dt * 1000L)
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }.entries.sortedBy { it.key }.take(5).map { (dayStartMs, dayItems) ->
        val max = dayItems.maxOf { it.main.temp }
        val min = dayItems.minOf { it.main.temp }
        val dominant = dayItems
            .mapNotNull { it.weather.firstOrNull()?.main }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key ?: "Clear"
        val diffDays = ((dayStartMs - today) / (24L * 60 * 60 * 1000)).toInt()
        val label = when (diffDays) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> {
                cal.timeInMillis = dayStartMs
                dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
            }
        }
        DailyForecast(
            dayLabel = label,
            dateMillis = dayStartMs,
            iconEmoji = weatherEmoji(dominant),
            condition = dominant,
            maxTemp = max,
            minTemp = min,
            items = dayItems
        )
    }
}
