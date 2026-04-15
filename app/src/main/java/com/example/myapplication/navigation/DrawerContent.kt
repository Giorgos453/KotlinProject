package com.example.myapplication.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text(
                text = "UPM Campus App",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, null) },
                label = { Text("Home") },
                selected = currentRoute == Screen.Home.route,
                onClick = {
                    onNavigate(Screen.Home.route)
                    onCloseDrawer()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.LocationOn, null) },
                label = { Text("Location") },
                selected = currentRoute == Screen.Location.route,
                onClick = {
                    onNavigate(Screen.Location.route)
                    onCloseDrawer()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Place, null) },
                label = { Text("Map") },
                selected = currentRoute == Screen.Map.route,
                onClick = {
                    onNavigate(Screen.Map.route)
                    onCloseDrawer()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Star, null) },
                label = { Text("Weather") },
                selected = currentRoute == Screen.Weather.route,
                onClick = {
                    onNavigate(Screen.Weather.route)
                    onCloseDrawer()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.CheckCircle, null) },
                label = { Text("Quiz") },
                selected = currentRoute == Screen.Quiz.route,
                onClick = {
                    onNavigate(Screen.Quiz.route)
                    onCloseDrawer()
                }
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ThumbUp, null) },
                label = { Text("Leaderboard") },
                selected = currentRoute == Screen.Leaderboard.route,
                onClick = {
                    onNavigate(Screen.Leaderboard.route)
                    onCloseDrawer()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Person, null) },
                label = { Text("Profile") },
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    onNavigate(Screen.Profile.route)
                    onCloseDrawer()
                }
            )
        }
    }
}
