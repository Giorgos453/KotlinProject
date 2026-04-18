package com.example.myapplication.data.profile

import com.example.myapplication.util.AppLogger
import com.example.myapplication.util.StageUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for user profile data backed by Firebase Realtime Database.
 */
class ProfileRepository(
    private val database: FirebaseDatabase
) {
    private val usersRef = database.reference.child("users")

    suspend fun getUserProfile(userId: String): Result<UserProfile> {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            val profile = snapshot.getValue(UserProfile::class.java)
                ?.copy(userId = userId)
                ?: UserProfile(userId = userId)
            Result.success(profile)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get profile for $userId", e)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<UserProfile> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                    ?.copy(userId = userId)
                    ?: UserProfile(userId = userId)
                trySend(profile)
            }

            override fun onCancelled(error: DatabaseError) {
                AppLogger.e(TAG, "Profile observation cancelled", error.toException())
                close(error.toException())
            }
        }

        usersRef.child(userId).addValueEventListener(listener)
        awaitClose { usersRef.child(userId).removeEventListener(listener) }
    }

    suspend fun updateProfile(profile: UserProfile): Result<Unit> {
        return try {
            val data = mapOf(
                "username" to profile.username,
                "email" to profile.email,
                "totalScore" to profile.totalScore,
                "quizzesCompleted" to profile.quizzesCompleted,
                "stage" to profile.stage,
                "createdAt" to profile.createdAt
            )
            usersRef.child(profile.userId).updateChildren(data).await()
            AppLogger.i(TAG, "Profile updated for ${profile.userId}")
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update profile", e)
            Result.failure(e)
        }
    }

    suspend fun addScoreAndIncrementQuiz(userId: String, points: Int): Result<Unit> {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            val current = snapshot.getValue(UserProfile::class.java) ?: UserProfile(userId = userId)
            val newScore = current.totalScore + points
            val newQuizCount = current.quizzesCompleted + 1
            val newStage = StageUtils.calculateStage(newScore)

            val updates = mapOf(
                "totalScore" to newScore,
                "quizzesCompleted" to newQuizCount,
                "stage" to newStage
            )
            usersRef.child(userId).updateChildren(updates).await()
            AppLogger.i(TAG, "Score updated for $userId: +$points -> total=$newScore, quizzes=$newQuizCount, stage=$newStage")
            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to add score for $userId", e)
            Result.failure(e)
        }
    }

    /**
     * Creates initial profile if it doesn't exist yet.
     */
    suspend fun ensureProfileExists(userId: String, username: String, email: String) {
        try {
            val snapshot = usersRef.child(userId).get().await()
            // Always make sure username + email + createdAt exist (for leaderboard display).
            val updates = mutableMapOf<String, Any>(
                "username" to username,
                "email" to email
            )
            if (!snapshot.child("createdAt").exists()) {
                updates["createdAt"] = System.currentTimeMillis()
            }
            if (!snapshot.child("xp").exists()) {
                updates["xp"] = snapshot.child("treeState").child("currentScore").getValue(Int::class.java) ?: 50
            }
            usersRef.child(userId).updateChildren(updates).await()
            if (!snapshot.exists()) {
                AppLogger.i(TAG, "Initial profile created for $userId")
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to ensure profile exists for $userId", e)
        }
    }

    suspend fun updateUsername(userId: String, username: String) {
        try {
            usersRef.child(userId).child("username").setValue(username).await()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update username for $userId", e)
        }
    }

    suspend fun updateAvatarImageId(userId: String, avatarImageId: String) {
        try {
            usersRef.child(userId).child("avatarImageId").setValue(avatarImageId).await()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update avatarImageId for $userId", e)
        }
    }

    companion object {
        private const val TAG = "ProfileRepository"
    }
}
