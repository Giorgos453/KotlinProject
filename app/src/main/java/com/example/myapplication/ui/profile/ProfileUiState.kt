package com.example.myapplication.ui.profile

import com.example.myapplication.data.airbuddy.model.AvatarStage
import com.example.myapplication.data.profile.UserProfile

/**
 * UI state for the profile screen.
 */
data class ProfileUiState(
    val profile: UserProfile? = null,
    val xp: Int = 0,
    val avatarStage: AvatarStage = AvatarStage.SPROUT,
    val loginStreak: Int = 0,
    val quizzesCompleted: Int = 0,
    val parksVisited: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)
