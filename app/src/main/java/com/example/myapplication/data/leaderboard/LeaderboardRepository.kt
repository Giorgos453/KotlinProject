package com.example.myapplication.data.leaderboard

import com.example.myapplication.data.airbuddy.scoreToAvatarStage
import com.example.myapplication.util.AppLogger
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Live leaderboard backed by Firebase RTDB at `users/{uid}`.
 * Uses `orderByChild("xp").limitToLast(N)` and reverses for descending order.
 */
class LeaderboardRepository(
    private val database: FirebaseDatabase
) {
    private val usersRef = database.reference.child("users")

    fun getTopLeaderboard(
        limit: Int = 20,
        currentUserId: String? = null
    ): Flow<Result<List<LeaderboardEntry>>> = callbackFlow {
        val query = usersRef.orderByChild("xp").limitToLast(limit)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = snapshot.children.mapNotNull { child ->
                    parseEntry(child, currentUserId)
                }
                    .sortedByDescending { it.xp }
                    .mapIndexed { index, entry -> entry.copy(rank = index + 1) }

                trySend(Result.success(entries))
            }

            override fun onCancelled(error: DatabaseError) {
                AppLogger.e(TAG, "Leaderboard query cancelled", error.toException())
                trySend(Result.failure(error.toException()))
            }
        }

        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun getCurrentUserEntry(userId: String): LeaderboardEntry? {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            parseEntry(snapshot, userId)?.copy(isCurrentUser = true)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load current user entry", e)
            null
        }
    }

    fun getUserRank(userId: String): Flow<Result<Int>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userXp = snapshot.child(userId).child("xp").getValue(Int::class.java) ?: 0
                val rank = snapshot.children.count { child ->
                    val xp = child.child("xp").getValue(Int::class.java) ?: 0
                    xp > userXp
                } + 1
                trySend(Result.success(rank))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }

        usersRef.addValueEventListener(listener)
        awaitClose { usersRef.removeEventListener(listener) }
    }

    private fun parseEntry(snapshot: DataSnapshot, currentUserId: String?): LeaderboardEntry? {
        val uid = snapshot.key ?: return null
        val xp = snapshot.child("xp").getValue(Int::class.java)
            ?: snapshot.child("treeState").child("currentScore").getValue(Int::class.java)
            ?: return null
        val rawUsername = snapshot.child("username").getValue(String::class.java)
        val email = snapshot.child("email").getValue(String::class.java)
        val username = when {
            !rawUsername.isNullOrBlank() -> rawUsername
            !email.isNullOrBlank() -> email.substringBefore("@")
            else -> "Player"
        }
        val avatarImageId = snapshot.child("avatarImageId").getValue(String::class.java) ?: ""
        return LeaderboardEntry(
            userId = uid,
            username = username,
            xp = xp,
            avatarStage = scoreToAvatarStage(xp),
            avatarImageId = avatarImageId,
            isCurrentUser = uid == currentUserId
        )
    }

    /**
     * Legacy helper kept so existing callers (Quiz flow) compile.
     * The leaderboard now reads from the `users/{uid}` mirror written by
     * TreeStateRepository, so explicit writes are no longer required.
     */
    suspend fun updateUserScore(userId: String, nickname: String, newScore: Int, stage: Int) {
        try {
            val updates = mapOf(
                "username" to nickname,
                "xp" to newScore
            )
            usersRef.child(userId).updateChildren(updates).await()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update user score for $userId", e)
        }
    }

    companion object {
        private const val TAG = "LeaderboardRepository"
    }
}
