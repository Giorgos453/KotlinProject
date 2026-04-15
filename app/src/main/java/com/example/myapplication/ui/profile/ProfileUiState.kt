package com.example.myapplication.ui.profile

import com.example.myapplication.data.profile.UserProfile

/**
 * UI state for the profile screen.
 */
data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
