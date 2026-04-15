package com.example.myapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Typsicheres Datenmodell für die Navigationselemente der Bottom Bar.
 * Icons und Labels sind hier zentral definiert – nicht in der Navigationslogik.
 */
enum class NavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val accessibilityLabel: String
) {
    HOME(
        route = Screen.Home.route,
        label = "Home",
        icon = Icons.Default.Home,
        accessibilityLabel = "Navigate to Home"
    ),
    DASHBOARD(
        route = Screen.Dashboard.route,
        label = "Dashboard",
        icon = Icons.Default.List,
        accessibilityLabel = "Navigate to Dashboard"
    ),
    LOCATION(
        route = Screen.Location.route,
        label = "Location",
        icon = Icons.Default.LocationOn,
        accessibilityLabel = "Navigate to Location"
    ),
    MAP(
        route = Screen.Map.route,
        label = "Map",
        icon = Icons.Default.Place,
        accessibilityLabel = "Navigate to Map"
    ),
    WEATHER(
        route = Screen.Weather.route,
        label = "Weather",
        icon = Icons.Default.Star,
        accessibilityLabel = "Navigate to Weather"
    ),
    QUIZ(
        route = Screen.Quiz.route,
        label = "Quiz",
        icon = Icons.Default.CheckCircle,
        accessibilityLabel = "Navigate to Quiz"
    ),
    LEADERBOARD(
        route = Screen.Leaderboard.route,
        label = "Ranking",
        icon = Icons.Default.ThumbUp,
        accessibilityLabel = "Navigate to Leaderboard"
    )
}

/**
 * Wiederverwendbare Bottom Navigation Bar.
 * Trennung von Navigationslogik (onItemSelected) und UI-Darstellung.
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemSelected: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        NavigationItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null // Label dient als Accessibility-Beschreibung
                    )
                },
                label = { Text(item.label) },
                modifier = Modifier.semantics {
                    contentDescription = item.accessibilityLabel
                }
            )
        }
    }
}
