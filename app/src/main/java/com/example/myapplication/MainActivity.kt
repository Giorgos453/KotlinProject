package com.example.myapplication

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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.geocoding.GeocodingRepository
import com.example.myapplication.data.location.LocationRepository
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.navigation.AppNavHost
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.ui.location.LocationViewModel
import com.example.myapplication.ui.map.MapViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.AppLogger
import com.google.android.gms.location.LocationServices
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {

    private lateinit var locationViewModelFactory: LocationViewModel.Factory
    private lateinit var mapViewModelFactory: MapViewModel.Factory
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i(TAG, "onCreate")

        // MapLibre muss vor der Erstellung einer MapView initialisiert werden.
        MapLibre.getInstance(this)

        // PreferencesManager als zentrale Repository-Schicht für alle SharedPreferences-Zugriffe
        preferencesManager = PreferencesManager(applicationContext)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRepository = LocationRepository(fusedLocationClient)
        // Context wird an die Factory übergeben, damit das ViewModel CSV-Dateien schreiben kann
        locationViewModelFactory = LocationViewModel.Factory(locationRepository, applicationContext)

        val geocodingRepository = GeocodingRepository(this)
        mapViewModelFactory = MapViewModel.Factory(locationRepository, geocodingRepository)

        val shouldShowDialog = !preferencesManager.hasUserName()

        enableEdgeToEdge()
        setContent {
            // Theme-Einstellungen aus PreferencesManager lesen
            val themeMode = remember { mutableStateOf(preferencesManager.themeMode) }
            val dynamicColors = remember { mutableStateOf(preferencesManager.dynamicColorsEnabled) }

            MyApplicationTheme(
                darkTheme = when (themeMode.value) {
                    "light" -> false
                    "dark" -> true
                    else -> androidx.compose.foundation.isSystemInDarkTheme()
                },
                dynamicColor = dynamicColors.value
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // State für den AlertDialog zur Benutzer-ID-Eingabe
                var showUserIdDialog by remember { mutableStateOf(shouldShowDialog) }
                var userIdInput by remember { mutableStateOf("") }
                // userName wird bei onResume aus PreferencesManager aktualisiert
                var userName by remember { mutableStateOf(preferencesManager.userName) }

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
                                        preferencesManager.userName = userIdInput.trim()
                                        AppLogger.i(TAG, "User ID saved: ${userIdInput.trim()}")
                                        userName = userIdInput.trim()
                                        showUserIdDialog = false
                                    }
                                }
                            ) {
                                Text("Save")
                            }
                        }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Bottom Navigation Bar – Tab-Wechsel mit State-Erhaltung
                        BottomNavBar(
                            currentRoute = currentRoute,
                            onItemSelected = { item ->
                                // userName bei jedem Tab-Wechsel aktualisieren
                                // (könnte in SettingsActivity geändert worden sein)
                                userName = preferencesManager.userName
                                themeMode.value = preferencesManager.themeMode
                                dynamicColors.value = preferencesManager.dynamicColorsEnabled

                                navController.navigate(item.route) {
                                    // Pop bis zum Start-Ziel, damit der Backstack nicht wächst
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Kein doppeltes Erstellen desselben Ziels
                                    launchSingleTop = true
                                    // Zustand beim Tab-Wechsel wiederherstellen
                                    restoreState = true
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        userName = userName,
                        onNameSaved = { name ->
                            preferencesManager.userName = name
                            userName = name
                        },
                        locationViewModelFactory = locationViewModelFactory,
                        mapViewModelFactory = mapViewModelFactory,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AppLogger.i(TAG, "onResume")
    }

    override fun onStart() {
        super.onStart()
        AppLogger.i(TAG, "onStart")
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
    }
}
