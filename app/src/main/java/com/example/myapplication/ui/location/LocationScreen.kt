package com.example.myapplication.ui.location

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.util.AppLogger

@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Zähler damit der Toast bei jedem Refresh erscheint, auch wenn Koordinaten gleich bleiben
    val toastTrigger = remember { mutableIntStateOf(0) }

    // Toast-Benachrichtigung bei jeder GPS-Koordinatenaktualisierung
    LaunchedEffect(uiState.latitude, uiState.longitude, toastTrigger.intValue) {
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("< Back")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Location",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Switch zum Aktivieren/Deaktivieren der CSV-Aufzeichnung
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.isRecordingEnabled) "Disable Location" else "Enable Location",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = uiState.isRecordingEnabled,
                onCheckedChange = { viewModel.toggleRecording(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!uiState.isRecordingEnabled) {
                // Nichts anzeigen wenn Location deaktiviert ist
            } else {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Getting location...")
                    }
                    uiState.error != null -> {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
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
                        }) {
                            Text("Retry")
                        }
                    }
                    uiState.latitude != null && uiState.longitude != null -> {
                        Text(
                            text = "Latitude",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${uiState.latitude}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Longitude",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${uiState.longitude}",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Altitude",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${"%.4f".format(uiState.altitude ?: 0.0)} m",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = {
                            viewModel.fetchLocation()
                            toastTrigger.intValue++
                        }) {
                            Text("Refresh")
                        }
                    }
                    else -> {
                        Text(
                            text = "Grant location permission to see your coordinates",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

private const val TAG = "LocationScreen"
