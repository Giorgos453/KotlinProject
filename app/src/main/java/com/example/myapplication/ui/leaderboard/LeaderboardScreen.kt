package com.example.myapplication.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.airbuddy.model.AvatarStage
import com.example.myapplication.data.airbuddy.stageDisplayName
import com.example.myapplication.data.leaderboard.LeaderboardEntry

private val screenBg = Color(0xFFFAFAF7)
private val primaryText = Color(0xFF2E4A32)
private val mutedText = Color(0xFF8B9590)
private val cardBg = Color(0xFFFFFFFF)
private val cardBorder = Color(0xFFEEEEEE)
private val accentGreen = Color(0xFF2E7D32)
private val accentGreenLight = Color(0xFF43A047)
private val mintGradientStart = Color(0xFFE8F5E9)
private val mintGradientEnd = Color(0xFFD4EDDA)
private val currentUserRowBg = Color(0xFFE8F5E9)

@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: LeaderboardViewModel
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading && state.entries.isEmpty() && state.currentUserEntry == null -> {
            Box(
                modifier = Modifier.fillMaxSize().background(screenBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = accentGreen)
            }
        }
        state.error != null && state.entries.isEmpty() -> {
            ErrorContent(
                message = state.error!!,
                onRetry = { viewModel.refreshLeaderboard() },
                onBack = onNavigateBack
            )
        }
        else -> {
            LeaderboardContent(
                state = state,
                onRefresh = { viewModel.refreshLeaderboard() }
            )
        }
    }
}

@Composable
private fun LeaderboardContent(
    state: LeaderboardUiState,
    onRefresh: () -> Unit
) {
    val currentUser = state.currentUserEntry
        ?: state.entries.firstOrNull { it.isCurrentUser }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Leaderboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryText
                )
                Text(
                    text = "Live ranking",
                    fontSize = 13.sp,
                    color = mutedText
                )
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.semantics { contentDescription = "Refresh leaderboard" }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = accentGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ── Current user summary card ──
        if (currentUser != null) {
            CurrentUserCard(
                entry = currentUser,
                rankOverride = state.userRank
            )
        }

        // ── ALL PLAYERS section ──
        if (state.entries.isNotEmpty()) {
            Spacer(modifier = Modifier.height(18.dp))
            SectionLabel("ALL PLAYERS")
        }

        if (state.entries.isEmpty() && currentUser != null) {
            // Only current user exists
            Spacer(modifier = Modifier.height(24.dp))
            EmptyStateCard(
                title = "You're the first player!",
                message = "Share the app with friends to compete on the leaderboard."
            )
        } else if (state.entries.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            EmptyStateCard(
                title = "No players yet",
                message = "Complete quizzes and visit parks to start climbing."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.entries,
                    key = { it.userId }
                ) { entry ->
                    LeaderboardItemRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun CurrentUserCard(
    entry: LeaderboardEntry,
    rankOverride: Int?
) {
    val rank = if (entry.rank > 0) entry.rank else rankOverride ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(mintGradientStart, mintGradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Rank pill
            RankPill(rank = rank, isCurrentUser = true)

            Spacer(modifier = Modifier.width(12.dp))

            // Avatar
            AvatarCircle(stage = entry.avatarStage, size = 44.dp)

            Spacer(modifier = Modifier.width(12.dp))

            // Username + stage
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.username.ifBlank { "You" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stageDisplayName(entry.avatarStage),
                    fontSize = 12.sp,
                    color = accentGreen
                )
            }

            // XP value
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.xp}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentGreen
                )
                Text(
                    text = "XP",
                    fontSize = 11.sp,
                    color = mutedText
                )
            }
        }
    }
}

@Composable
private fun LeaderboardItemRow(entry: LeaderboardEntry) {
    val isCurrentUser = entry.isCurrentUser
    val background = if (isCurrentUser) currentUserRowBg else cardBg
    val borderColor = if (isCurrentUser) accentGreen.copy(alpha = 0.35f) else cardBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .border(0.5.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RankPill(rank = entry.rank, isCurrentUser = isCurrentUser)

            Spacer(modifier = Modifier.width(12.dp))

            AvatarCircle(stage = entry.avatarStage, size = 32.dp)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.username,
                    fontSize = 14.sp,
                    fontWeight = if (isCurrentUser) FontWeight.SemiBold else FontWeight.Medium,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stageDisplayName(entry.avatarStage),
                    fontSize = 11.sp,
                    color = mutedText
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.xp}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentGreen
                )
                Text(
                    text = "XP",
                    fontSize = 10.sp,
                    color = mutedText
                )
            }
        }
    }
}

@Composable
private fun RankPill(
    rank: Int,
    isCurrentUser: Boolean
) {
    val (bg, fg) = when {
        rank == 1 -> Color(0xFFFFF4C7) to Color(0xFFB7860B) // gold
        rank == 2 -> Color(0xFFF1F1F1) to Color(0xFF6F6F6F) // silver
        rank == 3 -> Color(0xFFF4E1D1) to Color(0xFF9C5A2F) // bronze
        isCurrentUser -> accentGreen to Color.White
        else -> Color(0xFFF3F5F3) to mutedText
    }

    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (rank > 0) "#$rank" else "—",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}

@Composable
private fun AvatarCircle(stage: AvatarStage, size: androidx.compose.ui.unit.Dp) {
    val (emoji, bgColor) = stageEmojiAndBg(stage)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (size.value * 0.55f).sp
        )
    }
}

private fun stageEmojiAndBg(stage: AvatarStage): Pair<String, Color> = when (stage) {
    AvatarStage.WILTED -> "\uD83C\uDF42" to Color(0xFFF3E5AB)
    AvatarStage.SEED -> "\uD83C\uDF31" to Color(0xFFE8F5E9)
    AvatarStage.SPROUT -> "\uD83C\uDF3F" to Color(0xFFC8E6C9)
    AvatarStage.YOUNG_TREE -> "\uD83C\uDF32" to Color(0xFFA5D6A7)
    AvatarStage.HEALTHY_TREE -> "\uD83C\uDF33" to Color(0xFF81C784)
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = mutedText,
        letterSpacing = 1.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 6.dp)
    )
}

@Composable
private fun EmptyStateCard(
    title: String,
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(0.5.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = mutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFFC62828),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text("Retry", color = accentGreen)
        }
        TextButton(onClick = onBack) {
            Text("Back", color = mutedText)
        }
    }
}
