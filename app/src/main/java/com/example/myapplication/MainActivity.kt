package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.geocoding.GeocodingRepository
import com.example.myapplication.data.location.LocationRepository
import com.example.myapplication.navigation.AppNavHost
import com.example.myapplication.ui.location.LocationViewModel
import com.example.myapplication.ui.map.MapViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.AppLogger
import com.google.android.gms.location.LocationServices
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {

    private lateinit var locationViewModelFactory: LocationViewModel.Factory
    private lateinit var mapViewModelFactory: MapViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i(TAG, "onCreate")

        // MapLibre muss vor der Erstellung einer MapView initialisiert werden.
        MapLibre.getInstance(this)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRepository = LocationRepository(fusedLocationClient)
        // Context wird an die Factory übergeben, damit das ViewModel CSV-Dateien schreiben kann
        locationViewModelFactory = LocationViewModel.Factory(locationRepository, applicationContext)

        val geocodingRepository = GeocodingRepository(this)
        mapViewModelFactory = MapViewModel.Factory(locationRepository, geocodingRepository)

        // Prüfe, ob eine Benutzer-ID in SharedPreferences vorhanden ist
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = prefs.getString(KEY_USER_ID, null)
        val shouldShowDialog = savedUserId.isNullOrBlank()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                // State für den AlertDialog zur Benutzer-ID-Eingabe
                var showUserIdDialog by remember { mutableStateOf(shouldShowDialog) }
                var userIdInput by remember { mutableStateOf("") }

                // AlertDialog anzeigen, falls keine Benutzer-ID gespeichert ist
                if (showUserIdDialog) {
                    AlertDialog(
                        onDismissRequest = { /* Dialog kann nicht geschlossen werden ohne Eingabe */ },
                        title = { Text("User Identifier") },
                        text = {
                            OutlinedTextField(
                                value = userIdInput,
                                onValueChange = { userIdInput = it },
                                label = { Text("Enter your User ID") },
                                singleLine = true
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (userIdInput.isNotBlank()) {
                                        // Benutzer-ID in SharedPreferences speichern
                                        prefs.edit()
                                            .putString(KEY_USER_ID, userIdInput.trim())
                                            .apply()
                                        AppLogger.i(TAG, "User ID saved: ${userIdInput.trim()}")
                                        showUserIdDialog = false
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    )
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        locationViewModelFactory = locationViewModelFactory,
                        mapViewModelFactory = mapViewModelFactory,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppLogger.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        AppLogger.i(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        AppLogger.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        AppLogger.i(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.i(TAG, "onDestroy")
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PREFS_NAME = "app_prefs"
        private const val KEY_USER_ID = "user_id"
    }
}
