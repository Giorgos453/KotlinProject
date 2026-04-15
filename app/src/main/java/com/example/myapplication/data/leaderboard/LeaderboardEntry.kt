package com.example.myapplication.data.leaderboard

/**
 * Data model for a leaderboard entry stored in Firebase RTDB.
 * Default values required for Firebase deserialization.
 */
data class LeaderboardEntry(
    val userId: String = "",
    val nickname: String = "",
    val score: Int = 0,
    val stage: Int = 0,
    val rank: Int = 0
)
