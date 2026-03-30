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
import android.content.Intent
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import com.example.myapplication.SecondActivity
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
    viewModel: LocationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Toast nur bei manuellem Refresh anzeigen, nicht bei automatischen Updates
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                            showToastOnNextUpdate.intValue++
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

        // Button zum Öffnen des GPS-Logs (CSV Viewer)
        OutlinedButton(
            onClick = {
                context.startActivity(Intent(context, SecondActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View GPS Log")
        }
    }
}

private const val TAG = "LocationScreen"
