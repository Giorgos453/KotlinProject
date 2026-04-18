package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.myapplication.data.leaderboard.LeaderboardRepository
import com.example.myapplication.data.profile.ProfileRepository
import com.example.myapplication.data.quiz.QuizRepository
import com.example.myapplication.data.firebase.AuthRepository
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
import com.example.myapplication.navigation.DrawerContent
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.location.LocationViewModel
import com.example.myapplication.ui.map.MapViewModel
import com.example.myapplication.ui.leaderboard.LeaderboardViewModel
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.quiz.QuizViewModel
import com.example.myapplication.ui.airbuddy.AirQualityViewModel
import com.example.myapplication.data.airquality.AirQualityRepository
import com.example.myapplication.data.airbuddy.TreeStateRepository
import com.example.myapplication.data.airbuddy.TreeScoreManager
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.settings.SettingsActivity
import com.example.myapplication.ui.weather.WeatherViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.AppLogger
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.location.LocationServices
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {

    private lateinit var locationViewModelFactory: LocationViewModel.Factory
    private lateinit var mapViewModelFactory: MapViewModel.Factory
    private lateinit var weatherViewModelFactory: WeatherViewModel.Factory
    private lateinit var quizViewModelFactory: QuizViewModel.Factory
    private lateinit var leaderboardViewModelFactory: LeaderboardViewModel.Factory
    private lateinit var profileViewModelFactory: ProfileViewModel.Factory
    private lateinit var airQualityViewModelFactory: AirQualityViewModel.Factory
    private lateinit var homeViewModelFactory: HomeViewModel.Factory
    private lateinit var treeScoreManager: TreeScoreManager
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var userRepository: UserRepository
    private val authRepository = AuthRepository()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i(TAG, "onCreate")

        MapLibre.getInstance(this)

        preferencesManager = PreferencesManager(applicationContext)

        // initialize Room database and repositories
        val database = AppDatabase.getInstance(applicationContext)
        val gpsRepository = GpsCoordinateRepository(database.locationDao())
        val campusMarkerRepository = CampusMarkerRepository(database.campusMarkerDao())
        userRepository = UserRepository(database.userDao())

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRepository = LocationRepository(fusedLocationClient)
        // LocationViewModel now uses GpsCoordinateRepository instead of Context for CSV
        locationViewModelFactory = LocationViewModel.Factory(locationRepository, gpsRepository)

        val geocodingRepository = GeocodingRepository(this)

        // Weather: create repository and ViewModel factory
        val apiKeyManager = ApiKeyManager.getInstance(applicationContext)
        val weatherRepository = WeatherRepository(
            remoteDataSource = WeatherRemoteDataSource(),
            weatherCacheDao = database.weatherCacheDao(),
            apiKeyManager = apiKeyManager
        )
        weatherViewModelFactory = WeatherViewModel.Factory(weatherRepository, applicationContext)

        // Quiz: Repository and ViewModel factory
        val quizDatabase = com.example.myapplication.data.database.AppDatabase.getInstance(applicationContext)
        val quizRepository = QuizRepository(quizDatabase.quizQuestionDao(), applicationContext)
        // Leaderboard: Repository and ViewModel factory
        val leaderboardRepository = LeaderboardRepository(FirebaseDatabase.getInstance())
        val currentUserId = authRepository.currentUser?.uid
        val currentNickname = authRepository.currentUser?.displayName
            ?: authRepository.currentUser?.email
            ?: "Anonymous"

        // Profile repository needed for quiz score updates
        val profileRepository = ProfileRepository(FirebaseDatabase.getInstance())

        // Air quality and tree growth system
        val airQualityRepository = AirQualityRepository()
        airQualityViewModelFactory = AirQualityViewModel.Factory(airQualityRepository, locationRepository, applicationContext)

        val treeStateRepository = TreeStateRepository(FirebaseDatabase.getInstance(), quizDatabase.treeStateDao())
        profileViewModelFactory = ProfileViewModel.Factory(
            profileRepository,
            treeStateRepository,
            currentUserId,
            preferencesManager,
            applicationContext
        )
        treeScoreManager = TreeScoreManager(treeStateRepository, airQualityRepository)
        homeViewModelFactory = HomeViewModel.Factory(treeStateRepository, treeScoreManager, currentUserId)

        // MapViewModel needs tree state + score manager to support park check-ins
        mapViewModelFactory = MapViewModel.Factory(
            locationRepository,
            geocodingRepository,
            campusMarkerRepository,
            treeStateRepository,
            treeScoreManager,
            currentUserId
        )

        quizViewModelFactory = QuizViewModel.Factory(
            quizRepository, leaderboardRepository, profileRepository, treeScoreManager, currentUserId, currentNickname
        )
        leaderboardViewModelFactory = LeaderboardViewModel.Factory(leaderboardRepository, currentUserId)

        // Ensure profile exists in Firebase on first login
        if (currentUserId != null) {
            val email = authRepository.currentUser?.email ?: ""
            CoroutineScope(Dispatchers.IO).launch {
                profileRepository.ensureProfileExists(currentUserId, currentNickname, email)
            }
        }

        // API key check: if no key is stored, navigate directly to Settings
        if (!apiKeyManager.hasApiKey()) {
            AppLogger.i(TAG, "No API key found – redirecting to Settings")
            startActivity(SettingsActivity.newIntent(this))
        }

        val shouldShowDialog = !preferencesManager.hasUserName()

        // Display name from Firebase user (email or displayName)
        val firebaseUser = authRepository.currentUser
        val firebaseDisplayName = firebaseUser?.displayName
            ?: firebaseUser?.email
            ?: "User"

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

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        DrawerContent(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                userName = preferencesManager.userName
                                themeMode.value = preferencesManager.themeMode
                                dynamicColors.value = preferencesManager.dynamicColorsEnabled

                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onSettingsClick = {
                                startActivity(SettingsActivity.newIntent(this@MainActivity))
                            },
                            onCloseDrawer = { scope.launch { drawerState.close() } }
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text(firebaseDisplayName) },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open menu"
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { performLogout() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Logout"
                                        )
                                    }
                                }
                            )
                        },
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
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            locationViewModelFactory = locationViewModelFactory,
                            mapViewModelFactory = mapViewModelFactory,
                            weatherViewModelFactory = weatherViewModelFactory,
                            quizViewModelFactory = quizViewModelFactory,
                            leaderboardViewModelFactory = leaderboardViewModelFactory,
                            profileViewModelFactory = profileViewModelFactory,
                            airQualityViewModelFactory = airQualityViewModelFactory,
                            homeViewModelFactory = homeViewModelFactory,
                            treeScoreManager = treeScoreManager,
                            onLogout = { performLogout() },
                            onOpenSettings = { startActivity(SettingsActivity.newIntent(this@MainActivity)) },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
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

    /** Signs the user out of Firebase and returns to LoginActivity. */
    private fun performLogout() {
        authRepository.signOut()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    /** Persists the username in the Room database in parallel */
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
