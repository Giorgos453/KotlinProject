package com.example.myapplication.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val drawerBg = Color(0xFFFAFAF7)
private val primaryText = Color(0xFF2E4A32)
private val mutedText = Color(0xFF8B9590)
private val accentGreen = Color(0xFF2E7D32)
private val selectedBg = Color(0xFFE8F5E9)
private val borderColor = Color(0xFFEEEEEE)

@Composable
fun DrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = drawerBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            // Header area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "\uD83C\uDF31",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AirBuddy",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryText
                )
                Text(
                    text = "Madrid",
                    fontSize = 14.sp,
                    color = mutedText
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = borderColor)

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation items
            DrawerItem(
                emoji = "\uD83D\uDC64",
                label = "Profile",
                isSelected = currentRoute == Screen.Profile.route,
                onClick = {
                    onNavigate(Screen.Profile.route)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            DrawerItem(
                emoji = "\uD83D\uDCCD",
                label = "Location",
                isSelected = currentRoute == Screen.Location.route,
                onClick = {
                    onNavigate(Screen.Location.route)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            DrawerItem(
                emoji = "\u2699\uFE0F",
                label = "Settings",
                isSelected = false,
                onClick = {
                    onSettingsClick()
                    onCloseDrawer()
                }
            )

            // Footer pushed to bottom
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "AirBuddy Madrid v1.0",
                fontSize = 11.sp,
                color = mutedText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
private fun DrawerItem(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) selectedBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = emoji,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) accentGreen else primaryText
            )
        }
    }
}
