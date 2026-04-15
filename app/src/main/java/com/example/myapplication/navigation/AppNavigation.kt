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
import com.example.myapplication.ui.leaderboard.LeaderboardScreen
import com.example.myapplication.ui.leaderboard.LeaderboardViewModel
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.profile.ProfileViewModel
import com.example.myapplication.ui.quiz.QuizScreen
import com.example.myapplication.ui.quiz.QuizViewModel
import com.example.myapplication.ui.weather.WeatherScreen
import com.example.myapplication.ui.weather.WeatherViewModel

/**
 * Typsichere Routen-Definition als Sealed Class.
 * Kein Magic-String-Routing – alle Routen sind zentral definiert.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Dashboard : Screen("dashboard")
    data object Location : Screen("location")
    data object Map : Screen("map")
    data object Weather : Screen("weather")
    data object Quiz : Screen("quiz")
    data object Leaderboard : Screen("leaderboard")
    data object Profile : Screen("profile")
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
    weatherViewModelFactory: WeatherViewModel.Factory,
    quizViewModelFactory: QuizViewModel.Factory,
    leaderboardViewModelFactory: LeaderboardViewModel.Factory,
    profileViewModelFactory: ProfileViewModel.Factory,
    onLogout: () -> Unit,
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

        composable(Screen.Weather.route) {
            val weatherViewModel: WeatherViewModel = viewModel(factory = weatherViewModelFactory)
            WeatherScreen(viewModel = weatherViewModel)
        }

        composable(Screen.Quiz.route) {
            val quizViewModel: QuizViewModel = viewModel(factory = quizViewModelFactory)
            QuizScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = quizViewModel
            )
        }

        composable(Screen.Leaderboard.route) {
            val leaderboardViewModel: LeaderboardViewModel = viewModel(factory = leaderboardViewModelFactory)
            LeaderboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = leaderboardViewModel
            )
        }

        composable(Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(factory = profileViewModelFactory)
            ProfileScreen(
                onLogout = onLogout,
                viewModel = profileViewModel
            )
        }
    }
}
