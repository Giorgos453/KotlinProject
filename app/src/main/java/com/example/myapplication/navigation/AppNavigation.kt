package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.location.LocationScreen
import com.example.myapplication.ui.location.LocationViewModel
import com.example.myapplication.ui.map.MapScreen
import com.example.myapplication.ui.map.MapViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Dashboard : Screen("dashboard/{userName}") {
        fun createRoute(userName: String) = "dashboard/$userName"
    }
    data object Location : Screen("location")
    data object Map : Screen("map")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
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
                onNavigateToDashboard = { userName ->
                    navController.navigate(Screen.Dashboard.createRoute(userName))
                },
                onNavigateToLocation = {
                    navController.navigate(Screen.Location.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                }
            )
        }

        composable(
            route = Screen.Dashboard.route,
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            DashboardScreen(
                userName = userName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Location.route) {
            val locationViewModel: LocationViewModel = viewModel(factory = locationViewModelFactory)
            LocationScreen(
                viewModel = locationViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Map.route) {
            val mapViewModel: MapViewModel = viewModel(factory = mapViewModelFactory)
            MapScreen(
                viewModel = mapViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
