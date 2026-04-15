package com.example.myapplication.ui.leaderboard

import com.example.myapplication.data.leaderboard.LeaderboardEntry

/**
 * UI state for the leaderboard screen.
 */
data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUserId: String? = null,
    val userRank: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
