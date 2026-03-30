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
import com.example.myapplication.data.database.entity.UserEntity
import es.upm.btb.helloworldkt.persistence.room.AppDatabase
import com.example.myapplication.data.database.repository.CampusMarkerRepository
import com.example.myapplication.data.database.repository.GpsCoordinateRepository
import com.example.myapplication.data.database.repository.UserRepository
import com.example.myapplication.data.geocoding.GeocodingRepository
import com.example.myapplication.data.network.WeatherRemoteDataSource
import com.example.myapplication.data.network.WeatherRepository
import com.example.myapplication.data.security.ApiKeyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.myapplication.data.location.LocationRepository
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.navigation.AppNavHost
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.ui.location.LocationViewModel
import com.example.myapplication.ui.map.MapViewModel
import com.example.myapplication.ui.settings.SettingsActivity
import com.example.myapplication.ui.weather.WeatherViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.AppLogger
import com.google.android.gms.location.LocationServices
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {

    private lateinit var locationViewModelFactory: LocationViewModel.Factory
    private lateinit var mapViewModelFactory: MapViewModel.Factory
    private lateinit var weatherViewModelFactory: WeatherViewModel.Factory
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i(TAG, "onCreate")

        MapLibre.getInstance(this)

        preferencesManager = PreferencesManager(applicationContext)

        // Room-Datenbank und Repositories initialisieren
        val database = AppDatabase.getInstance(applicationContext)
        val gpsRepository = GpsCoordinateRepository(database.locationDao())
        val campusMarkerRepository = CampusMarkerRepository(database.campusMarkerDao())
        userRepository = UserRepository(database.userDao())

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRepository = LocationRepository(fusedLocationClient)
        // LocationViewModel nutzt jetzt GpsCoordinateRepository statt Context fuer CSV
        locationViewModelFactory = LocationViewModel.Factory(locationRepository, gpsRepository)

        val geocodingRepository = GeocodingRepository(this)
        // MapViewModel nutzt jetzt CampusMarkerRepository statt statische CampusTourData
        mapViewModelFactory = MapViewModel.Factory(locationRepository, geocodingRepository, campusMarkerRepository)

        // Weather: Repository und ViewModel-Factory erstellen
        val apiKeyManager = ApiKeyManager.getInstance(applicationContext)
        val weatherRepository = WeatherRepository(
            remoteDataSource = WeatherRemoteDataSource(),
            weatherCacheDao = database.weatherCacheDao(),
            apiKeyManager = apiKeyManager
        )
        weatherViewModelFactory = WeatherViewModel.Factory(weatherRepository, applicationContext)

        // API-Key-Check: Falls kein Key vorhanden, direkt zu Settings navigieren
        if (!apiKeyManager.hasApiKey()) {
            AppLogger.i(TAG, "No API key found – redirecting to Settings")
            startActivity(SettingsActivity.newIntent(this))
        }

        val shouldShowDialog = !preferencesManager.hasUserName()

        enableEdgeToEdge()
        setContent {
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

                var showUserIdDialog by remember { mutableStateOf(shouldShowDialog) }
                var userIdInput by remember { mutableStateOf("") }
                var userName by remember { mutableStateOf(preferencesManager.userName) }

                if (showUserIdDialog) {
                    AlertDialog(
                        onDismissRequest = { },
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
                                        val trimmed = userIdInput.trim()
                                        preferencesManager.userName = trimmed
                                        saveUserToDatabase(trimmed)
                                        AppLogger.i(TAG, "User ID saved: $trimmed")
                                        userName = trimmed
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
                        BottomNavBar(
                            currentRoute = currentRoute,
                            onItemSelected = { item ->
                                userName = preferencesManager.userName
                                themeMode.value = preferencesManager.themeMode
                                dynamicColors.value = preferencesManager.dynamicColorsEnabled

                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
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
                            saveUserToDatabase(name)
                            userName = name
                        },
                        locationViewModelFactory = locationViewModelFactory,
                        mapViewModelFactory = mapViewModelFactory,
                        weatherViewModelFactory = weatherViewModelFactory,
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

    /** Speichert den Benutzernamen parallel in der Room-Datenbank */
    private fun saveUserToDatabase(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userRepository.insert(UserEntity(name = name))
                AppLogger.i(TAG, "User saved to database: $name")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error saving user to database", e)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
