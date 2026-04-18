package com.example.myapplication.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavigationItem(
    val route: String,
    val label: String,
    val emoji: String,
    val accessibilityLabel: String
) {
    HOME(
        route = Screen.Home.route,
        label = "Home",
        emoji = "\uD83C\uDFE0",
        accessibilityLabel = "Navigate to Home"
    ),
    MAP(
        route = Screen.Map.route,
        label = "Map",
        emoji = "\uD83D\uDCCD",
        accessibilityLabel = "Navigate to Map"
    ),
    WEATHER(
        route = Screen.Weather.route,
        label = "Weather",
        emoji = "\u2601\uFE0F",
        accessibilityLabel = "Navigate to Weather"
    ),
    AIR_QUALITY(
        route = Screen.AirQuality.route,
        label = "Air",
        emoji = "\uD83C\uDF43",
        accessibilityLabel = "Navigate to Air Quality"
    )
}

private val activeColor = Color(0xFF2E7D32)
private val inactiveLabel = Color(0xFF8B9590)
private val borderColor = Color(0xFFEEEEEE)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemSelected: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HorizontalDivider(thickness = 0.5.dp, color = borderColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 12.dp, bottom = 4.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationItem.entries.forEach { item ->
                val isSelected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onItemSelected(item) }
                        .semantics { contentDescription = item.accessibilityLabel }
                ) {
                    Text(
                        text = item.emoji,
                        fontSize = 18.sp,
                        color = if (isSelected) Color.Unspecified else Color.Unspecified.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) activeColor else inactiveLabel,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
