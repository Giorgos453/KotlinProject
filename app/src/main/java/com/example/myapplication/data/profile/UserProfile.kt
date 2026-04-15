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
    val createdAt: Long = System.currentTimeMillis()
)
