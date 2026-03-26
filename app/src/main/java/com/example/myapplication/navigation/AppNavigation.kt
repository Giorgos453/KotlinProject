package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.location.LocationScreen
import com.example.myapplication.ui.location.LocationViewModel
import com.example.myapplication.ui.map.MapScreen
import com.example.myapplication.ui.map.MapViewModel

/**
 * Typsichere Routen-Definition als Sealed Class.
 * Kein Magic-String-Routing – alle Routen sind zentral definiert.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Dashboard : Screen("dashboard")
    data object Location : Screen("location")
    data object Map : Screen("map")
}

/**
 * Zentraler NavHost für alle Compose-Screens.
 * Navigationslogik ist von den UI-Komponenten getrennt –
 * Screens erhalten keine Navigations-Callbacks mehr für Tab-Wechsel.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    userName: String,
    onNameSaved: (String) -> Unit,
    locationViewModelFactory: LocationViewModel.Factory,
    mapViewModelFactory: MapViewModel.Factory,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                userName = userName,
                onNameSaved = onNameSaved
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(userName = userName)
        }

        composable(Screen.Location.route) {
            val locationViewModel: LocationViewModel = viewModel(factory = locationViewModelFactory)
            LocationScreen(viewModel = locationViewModel)
        }

        composable(Screen.Map.route) {
            val mapViewModel: MapViewModel = viewModel(factory = mapViewModelFactory)
            MapScreen(viewModel = mapViewModel)
        }
    }
}
