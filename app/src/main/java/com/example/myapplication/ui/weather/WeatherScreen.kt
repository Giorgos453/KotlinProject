package com.example.myapplication.ui.weather

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.database.entity.WeatherCacheEntity
import com.example.myapplication.data.network.model.ForecastItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val screenBg = Color(0xFFFAFAF7)
private val cardBg = Color(0xFFFFFFFF)
private val cardBorder = Color(0xFFEEEEEE)
private val primaryText = Color(0xFF2E4A32)
private val mutedText = Color(0xFF8B9590)
private val accentGreen = Color(0xFF2E7D32)
private val lightGreenBg = Color(0xFFE8F5E9)
private val heroStart = Color(0xFFE8F5E9)
private val heroEnd = Color(0xFFD4EDDA)
private val heroText = Color(0xFF1B5E20)
private val heroSubText = Color(0xFF558B2F)
private val errorRed = Color(0xFFC62828)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            fetchLocationAndLoadWeather(context, viewModel)
        } else {
            viewModel.loadWeather(MADRID_LAT, MADRID_LON)
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState is WeatherUiState.Loading,
        onRefresh = {
            fetchLocationAndLoadWeather(context, viewModel, forceRefresh = true)
        },
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // B) Header
            WeatherHeader(
                location = (uiState as? WeatherUiState.Success)?.weather?.cityName,
                onRefresh = {
                    fetchLocationAndLoadWeather(context, viewModel, forceRefresh = true)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            when (val state = uiState) {
                is WeatherUiState.Initial -> {
                    if (!locationPermission.status.isGranted) {
                        PermissionRequestCard(
                            onRequest = { locationPermission.launchPermissionRequest() }
                        )
                    } else {
                        LoadingBlock()
                    }
                }

                is WeatherUiState.Loading -> LoadingBlock()

                is WeatherUiState.Success -> {
                    WeatherContent(
                        weather = state.weather,
                        forecast = forecast
                    )
                }

                is WeatherUiState.Error -> {
                    ErrorCard(
                        message = state.message,
                        onRetry = { fetchLocationAndLoadWeather(context, viewModel, forceRefresh = true) }
                    )
                }

                is WeatherUiState.NoApiKey -> NoApiKeyCard(onOpenSettings = onOpenSettings)
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun WeatherHeader(location: String?, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Weather",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = location ?: "Locating...",
                fontSize = 14.sp,
                color = mutedText,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cardBg)
                .border(0.5.dp, cardBorder, CircleShape)
                .clickable(onClick = onRefresh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = accentGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WeatherContent(
    weather: WeatherCacheEntity,
    forecast: List<ForecastItem>
) {
    val dailyList = remember(forecast) { groupForecastByDay(forecast) }
    var selectedHourly by remember { mutableStateOf<ForecastItem?>(null) }
    var expandedDayMs by remember { mutableStateOf<Long?>(null) }

    // C) Hero current weather card
    HeroCard(weather = weather)

    Spacer(modifier = Modifier.height(22.dp))

    if (forecast.isNotEmpty()) {
        SectionLabel("TODAY'S FORECAST")
        Spacer(modifier = Modifier.height(10.dp))

        // E) Hourly horizontal
        val now = System.currentTimeMillis()
        val nextEight = remember(forecast) {
            forecast.filter { it.dt * 1000L >= now - (90 * 60 * 1000L) }.take(8)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(nextEight) { item ->
                val itemMs = item.dt * 1000L
                HourlyCard(
                    item = item,
                    isNow = isSameHour(itemMs, now),
                    onClick = { selectedHourly = item }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        SectionLabel("5-DAY FORECAST")
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            dailyList.forEach { daily ->
                DailyCard(
                    daily = daily,
                    isExpanded = expandedDayMs == daily.dateMillis,
                    onToggle = {
                        expandedDayMs = if (expandedDayMs == daily.dateMillis) null else daily.dateMillis
                    },
                    onHourlyClick = { selectedHourly = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
    }

    SectionLabel("DETAILS")
    Spacer(modifier = Modifier.height(10.dp))
    DetailsGrid(weather)

    if (selectedHourly != null) {
        HourlyDetailSheet(item = selectedHourly!!, onDismiss = { selectedHourly = null })
    }
}

@Composable
private fun HeroCard(weather: WeatherCacheEntity) {
    val main = weather.description.replaceFirstChar { it.uppercase() }
    val emoji = weatherEmojiFromDescription(weather.description, weather.iconCode)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(heroStart, heroEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 60.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${weather.temperature.toInt()}\u00B0",
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = heroText
            )
            Text(
                text = main,
                fontSize = 16.sp,
                color = heroSubText,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Feels like ${weather.feelsLike.toInt()}\u00B0",
                fontSize = 12.sp,
                color = heroSubText
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = mutedText,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun HourlyCard(item: ForecastItem, isNow: Boolean, onClick: () -> Unit) {
    val bg = if (isNow) lightGreenBg else cardBg
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeLabel = timeFmt.format(Date(item.dt * 1000L))
    val emoji = weatherEmoji(item.weather.firstOrNull()?.main)
    Box(
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isNow) "Now" else timeLabel,
                fontSize = 11.sp,
                color = if (isNow) accentGreen else mutedText,
                fontWeight = if (isNow) FontWeight.Medium else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${item.main.temp.toInt()}\u00B0",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText
            )
        }
    }
}

@Composable
private fun DailyCard(
    daily: DailyForecast,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onHourlyClick: (ForecastItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = daily.dayLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText,
                modifier = Modifier.weight(1f)
            )
            Text(text = daily.iconEmoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "${daily.maxTemp.toInt()}\u00B0 / ${daily.minTemp.toInt()}\u00B0",
                fontSize = 14.sp,
                color = primaryText
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(daily.items) { hItem ->
                        HourlyCard(
                            item = hItem,
                            isNow = false,
                            onClick = { onHourlyClick(hItem) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsGrid(weather: WeatherCacheEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailCard(
                emoji = "\uD83E\uDD14",
                label = "FEELS LIKE",
                value = "${weather.feelsLike.toInt()}\u00B0",
                modifier = Modifier.weight(1f)
            )
            DetailCard(
                emoji = "\uD83D\uDCA7",
                label = "HUMIDITY",
                value = "${weather.humidity}%",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailCard(
                emoji = "\uD83D\uDCA8",
                label = "WIND",
                value = "${weather.windSpeed} m/s",
                modifier = Modifier.weight(1f)
            )
            DetailCard(
                emoji = "\uD83C\uDF21\uFE0F",
                label = "PRESSURE",
                value = "${weather.pressure} hPa",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailCard(emoji: String, label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = mutedText,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HourlyDetailSheet(item: ForecastItem, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val timeFmt = remember { SimpleDateFormat("EEE HH:mm", Locale.getDefault()) }
    val timeLabel = timeFmt.format(Date(item.dt * 1000L))
    val condition = item.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "N/A"
    val emoji = weatherEmoji(item.weather.firstOrNull()?.main)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = timeLabel, fontSize = 14.sp, color = mutedText)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = emoji, fontSize = 56.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.main.temp.toInt()}\u00B0",
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = primaryText
            )
            Text(text = condition, fontSize = 14.sp, color = heroSubText)
            Spacer(modifier = Modifier.height(18.dp))

            SheetDetailRow("Feels like", "${item.main.feelsLike.toInt()}\u00B0")
            SheetDetailRow("Humidity", "${item.main.humidity}%")
            SheetDetailRow("Pressure", "${item.main.pressure} hPa")
            SheetDetailRow("Wind", "${item.wind?.speed ?: 0.0} m/s")
        }
    }
}

@Composable
private fun SheetDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = mutedText)
        Text(text = value, fontSize = 14.sp, color = primaryText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LoadingBlock() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = accentGreen)
    }
}

@Composable
private fun PermissionRequestCard(onRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Location permission needed",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Grant location to see weather for your area.",
                fontSize = 12.sp,
                color = mutedText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(accentGreen)
                    .clickable(onClick = onRequest)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Grant permission", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = errorRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = message, fontSize = 13.sp, color = errorRed, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(accentGreen)
                    .clickable(onClick = onRetry)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(text = "Retry", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun NoApiKeyCard(onOpenSettings: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "API key required",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add your OpenWeatherMap API key to load weather data.",
                fontSize = 12.sp,
                color = mutedText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(accentGreen)
                    .clickable(onClick = onOpenSettings)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Open Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun weatherEmojiFromDescription(description: String, iconCode: String): String {
    val d = description.lowercase()
    return when {
        d.contains("thunder") -> "\u26C8\uFE0F"
        d.contains("snow") -> "\u2744\uFE0F"
        d.contains("rain") || d.contains("drizzle") -> "\uD83C\uDF27\uFE0F"
        d.contains("mist") || d.contains("fog") || d.contains("haze") -> "\uD83C\uDF2B\uFE0F"
        d.contains("cloud") -> "\u2601\uFE0F"
        d.contains("clear") -> if (iconCode.endsWith("n")) "\uD83C\uDF11" else "\u2600\uFE0F"
        else -> "\uD83C\uDF24\uFE0F"
    }
}

@Suppress("MissingPermission")
private fun fetchLocationAndLoadWeather(
    context: android.content.Context,
    viewModel: WeatherViewModel,
    forceRefresh: Boolean = false
) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationToken = CancellationTokenSource()

    fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationToken.token)
        .addOnSuccessListener { location ->
            if (location != null) {
                viewModel.loadWeather(location.latitude, location.longitude, forceRefresh)
            } else {
                fusedClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) {
                        viewModel.loadWeather(lastLocation.latitude, lastLocation.longitude, forceRefresh)
                    } else {
                        viewModel.loadWeather(MADRID_LAT, MADRID_LON, forceRefresh)
                    }
                }
            }
        }
}

private fun isSameHour(aMs: Long, bMs: Long): Boolean {
    val ca = Calendar.getInstance().apply { timeInMillis = aMs }
    val cb = Calendar.getInstance().apply { timeInMillis = bMs }
    return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR) &&
            ca.get(Calendar.HOUR_OF_DAY) / 3 == cb.get(Calendar.HOUR_OF_DAY) / 3
}

private const val MADRID_LAT = 40.4165
private const val MADRID_LON = -3.7026
