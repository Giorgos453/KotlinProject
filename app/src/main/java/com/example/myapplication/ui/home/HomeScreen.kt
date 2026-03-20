package com.example.myapplication.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.util.AppLogger

@Composable
fun HomeScreen(
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToMap: () -> Unit = {}
) {
    var userName by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Enter your name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                AppLogger.d(TAG, "Navigating to dashboard with name: $userName")
                onNavigateToDashboard(userName.ifBlank { "User" })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Dashboard")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                AppLogger.d(TAG, "Navigating to location screen")
                onNavigateToLocation()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Location")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                AppLogger.d(TAG, "Navigating to map screen")
                onNavigateToMap()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Campus Map")
        }

    }
}

private const val TAG = "HomeScreen"
