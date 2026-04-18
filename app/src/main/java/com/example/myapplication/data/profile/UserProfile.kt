package com.example.myapplication.data.profile

/**
 * User profile stored in Firebase RTDB under "users/{userId}".
 * Default values required for Firebase deserialization.
 */
data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val totalScore: Int = 0,
    val quizzesCompleted: Int = 0,
    val stage: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val xp: Int = 0,
    val avatarStage: String = "SPROUT",
    val currentLoginStreak: Int = 0,
    val longestLoginStreak: Int = 0,
    val totalQuizzesCompleted: Int = 0,
    val visitedParkIds: String = "",
    val avatarImageId: String = "",
    val lastLoginDate: Long = 0L,
    val lastQuizDate: Long = 0L
)
