package com.example.myapplication.data.leaderboard

import com.example.myapplication.data.airbuddy.model.AvatarStage

/**
 * Live leaderboard entry derived from `users/{uid}` in Firebase RTDB.
 */
data class LeaderboardEntry(
    val userId: String = "",
    val username: String = "",
    val xp: Int = 0,
    val avatarStage: AvatarStage = AvatarStage.SPROUT,
    val avatarImageId: String = "",
    val isCurrentUser: Boolean = false,
    val rank: Int = 0
)
