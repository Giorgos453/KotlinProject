package com.example.myapplication.ui.location

import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.SecondActivity
import com.example.myapplication.util.AppLogger

private val screenBg = Color(0xFFFAFAF7)
private val primaryText = Color(0xFF2E4A32)
private val mutedText = Color(0xFF8B9590)
private val cardBg = Color(0xFFFFFFFF)
private val cardBorder = Color(0xFFEEEEEE)
private val accentGreen = Color(0xFF2E7D32)
private val trackingGreen = Color(0xFF4CAF50)
private val errorRed = Color(0xFFC62828)

@Composable
fun LocationScreen(
    viewModel: LocationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val showToastOnNextUpdate = remember { mutableIntStateOf(0) }

    LaunchedEffect(showToastOnNextUpdate.intValue) {
        if (showToastOnNextUpdate.intValue > 0) {
            val lat = uiState.latitude
            val lon = uiState.longitude
            if (lat != null && lon != null) {
                Toast.makeText(
                    context,
                    "New location: $lat, $lon",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        AppLogger.i(TAG, "Permission callback: granted=$granted")
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        if (!uiState.permissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        // ── B) Header ──
        Text(
            text = "Location",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = primaryText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Your current position",
            fontSize = 13.sp,
            color = mutedText,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ── C) Location card ──
        LocationCard(
            isRecordingEnabled = uiState.isRecordingEnabled,
            latitude = uiState.latitude,
            longitude = uiState.longitude,
            altitude = uiState.altitude,
            isLoading = uiState.isLoading,
            error = uiState.error,
            onToggle = { viewModel.toggleRecording(it) },
            onRetry = {
                if (uiState.permissionGranted) {
                    viewModel.fetchLocation()
                } else {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── D) Actions ──
        if (uiState.isRecordingEnabled) {
            PrimaryButton(
                label = "Refresh",
                onClick = {
                    viewModel.fetchLocation()
                    showToastOnNextUpdate.intValue++
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedActionButton(
            label = "View GPS log",
            onClick = {
                context.startActivity(Intent(context, SecondActivity::class.java))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LocationCard(
    isRecordingEnabled: Boolean,
    latitude: Double?,
    longitude: Double?,
    altitude: Double?,
    isLoading: Boolean,
    error: String?,
    onToggle: (Boolean) -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top row: label + toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Location tracking",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryText
                )
                Switch(
                    checked = isRecordingEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = trackingGreen,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFBDBDBD)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                !isRecordingEnabled -> {
                    OffState()
                }
                isLoading && latitude == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = accentGreen)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Getting location...",
                                fontSize = 12.sp,
                                color = mutedText
                            )
                        }
                    }
                }
                error != null -> {
                    ErrorState(error = error, onRetry = onRetry)
                }
                latitude != null && longitude != null -> {
                    LocationStats(latitude, longitude, altitude)
                }
                else -> {
                    Text(
                        text = "Grant location permission to see your coordinates.",
                        fontSize = 13.sp,
                        color = mutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun OffState() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F1F1)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOff,
                contentDescription = null,
                tint = mutedText,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Column {
            Text(
                text = "Location tracking is off",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText
            )
            Text(
                text = "Enable to see your coordinates",
                fontSize = 11.sp,
                color = mutedText
            )
        }
    }
}

@Composable
private fun LocationStats(
    latitude: Double,
    longitude: Double,
    altitude: Double?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = accentGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = "Live coordinates",
            fontSize = 12.sp,
            color = mutedText
        )
    }
    StatRow("Latitude", "%.6f".format(latitude))
    Spacer(modifier = Modifier.height(10.dp))
    StatRow("Longitude", "%.6f".format(longitude))
    Spacer(modifier = Modifier.height(10.dp))
    StatRow("Altitude", "%.2f m".format(altitude ?: 0.0))
}

@Composable
private fun StatRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = mutedText,
            letterSpacing = 0.3.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = primaryText
        )
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    Column {
        Text(
            text = error,
            fontSize = 13.sp,
            color = errorRed
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Retry",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = accentGreen,
            modifier = Modifier
                .clickable(onClick = onRetry)
                .padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(accentGreen)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OutlinedActionButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(0.5.dp, accentGreen, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = accentGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

private const val TAG = "LocationScreen"
