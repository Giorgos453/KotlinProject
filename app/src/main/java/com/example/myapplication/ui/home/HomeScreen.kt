package com.example.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.airbuddy.model.AvatarStage
import com.example.myapplication.ui.airbuddy.AirBuddyAvatar

private val screenBg = Color(0xFFFAFAF7)
private val heroGradientStart = Color(0xFFE8F5E9)
private val heroGradientEnd = Color(0xFFD4EDDA)
private val decorCircleColor = Color(0xFF4CAF50)
private val badgeBg = Color(0xFF2E7D32)
private val buddyNameColor = Color(0xFF558B2F)
private val scoreColor = Color(0xFF1B5E20)
private val scoreSubtitle = Color(0xFF558B2F)
private val progressTrack = Color(0xFF2E7D32).copy(alpha = 0.15f)
private val progressFill = Color(0xFF43A047)
private val progressLabel = Color(0xFF558B2F)
private val sectionLabel = Color(0xFF8B9590)
private val statNumber = Color(0xFF2E4A32)
private val statLabel = Color(0xFF8B9590)
private val cardBg = Color(0xFFFFFFFF)
private val cardBorder = Color(0xFFEEEEEE)
private val tileLabel = Color(0xFF2E4A32)
private val quizBadgeBg = Color(0xFFF3E5F5)
private val leaderboardBadgeBg = Color(0xFFFFF3E0)
private val howItWorksBadgeBg = Color(0xFFE3F2FD)

@Composable
fun HomeScreen(
    userName: String,
    onNameSaved: (String) -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Inactivity warning
        state.inactivityMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.dismissInactivityMessage() }) {
                        Text("OK")
                    }
                }
            }
        }

        // AQI drain warning
        state.aqiDrainMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Forest,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.dismissAqiMessage() }) {
                        Text("OK")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Hero Card ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(heroGradientStart, heroGradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Decorative circles
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-20).dp)
                    .background(decorCircleColor.copy(alpha = 0.08f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 30.dp)
                    .background(decorCircleColor.copy(alpha = 0.05f), CircleShape)
            )

            // Stage badge top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeBg)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stageName(state.avatarStage).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    letterSpacing = 0.3.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Tree emoji / avatar — clickable to open TreeDetailScreen
                Box(modifier = Modifier.clickable { onNavigate("treedetail") }) {
                    AirBuddyAvatar(
                        stage = state.avatarStage,
                        nickname = "",
                        modifier = Modifier.size(140.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // "MY AIRBUDDY"
                Text(
                    text = "MY AIRBUDDY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = buddyNameColor,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // XP number + unit
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${state.score}",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = scoreColor,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "XP",
                        fontSize = 16.sp,
                        color = scoreSubtitle,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Progress bar (to next stage, not to 100)
                LinearProgressIndicator(
                    progress = { state.stageProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = progressFill,
                    trackColor = progressTrack,
                    strokeCap = StrokeCap.Round,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (state.isHealthyTree) {
                        "Healthy tree – keep growing!"
                    } else {
                        "${state.pointsToNextStage} XP to ${state.nextStageName}"
                    },
                    fontSize = 11.sp,
                    color = progressLabel,
                    letterSpacing = 0.3.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── YOUR JOURNEY ──
        SectionLabel("YOUR JOURNEY")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Day Streak
            StatCard(
                emoji = "\uD83D\uDD25",
                value = "${state.loginStreak}",
                label = "Day streak",
                modifier = Modifier.weight(1f)
            )
            // Quizzes
            StatCard(
                emoji = null,
                value = "${state.quizzesCompleted}",
                label = "Quizzes",
                modifier = Modifier.weight(1f)
            )
            // Parks
            StatCard(
                emoji = null,
                value = "${state.parksVisited}",
                label = "Parks",
                modifier = Modifier.weight(1f)
            )
        }

        // ── CTA when score < 50 ──
        if (state.score < 50) {
            Spacer(modifier = Modifier.height(22.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFF9C4))
                    .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Your tree needs help!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = statNumber,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Play a quiz or visit a park to earn points.",
                        fontSize = 11.sp,
                        color = sectionLabel,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigate("quiz") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Play Quiz") }
                        Button(
                            onClick = { onNavigate("map") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Visit Map") }
                    }
                }
            }
        }

        // ── EXPLORE ──
        SectionLabel("EXPLORE")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExploreTile(
                emoji = "\u2753",
                badgeBg = quizBadgeBg,
                label = "Eco Quiz",
                onClick = { onNavigate("quiz") },
                modifier = Modifier.weight(1f)
            )
            ExploreTile(
                emoji = "\uD83C\uDFC6",
                badgeBg = leaderboardBadgeBg,
                label = "Leaderboard",
                onClick = { onNavigate("leaderboard") },
                modifier = Modifier.weight(1f)
            )
            ExploreTile(
                emoji = "\uD83D\uDCA1",
                badgeBg = howItWorksBadgeBg,
                label = "How it Works",
                onClick = { onNavigate("howitworks") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Spacer(modifier = Modifier.height(22.dp))
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = sectionLabel,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun StatCard(
    emoji: String?,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (emoji != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = value,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = statNumber
                    )
                }
            } else {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = statNumber
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = statLabel,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun ExploreTile(
    emoji: String,
    badgeBg: Color,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = tileLabel,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun stageName(stage: AvatarStage): String = when (stage) {
    AvatarStage.SEED -> "Seed"
    AvatarStage.SPROUT -> "Sprout"
    AvatarStage.YOUNG_TREE -> "Young Tree"
    AvatarStage.HEALTHY_TREE -> "Healthy Tree"
    AvatarStage.WILTED -> "Wilted"
}
