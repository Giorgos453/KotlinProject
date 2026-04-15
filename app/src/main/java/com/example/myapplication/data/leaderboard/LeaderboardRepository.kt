package com.example.myapplication.data.leaderboard

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
 * Repository for leaderboard data backed by Firebase Realtime Database.
 */
class LeaderboardRepository(
    private val database: FirebaseDatabase
) {
    private val leaderboardRef = database.reference.child("leaderboard")

    /**
     * Observes the top [limit] leaderboard entries in real-time, sorted by score descending.
     */
    fun getTopLeaderboard(limit: Int = 20): Flow<Result<List<LeaderboardEntry>>> = callbackFlow {
        val query = leaderboardRef.orderByChild("score").limitToLast(limit)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = snapshot.children.mapNotNull { child ->
                    child.getValue(LeaderboardEntry::class.java)
                        ?.copy(userId = child.key ?: "")
                }.reversed() // Firebase returns ascending; we need descending
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

    /**
     * Returns the rank of a specific user by counting how many users have a higher score.
     */
    fun getUserRank(userId: String): Flow<Result<Int>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userScore = snapshot.child(userId).getValue(LeaderboardEntry::class.java)?.score ?: 0
                val rank = snapshot.children.count { child ->
                    val entry = child.getValue(LeaderboardEntry::class.java)
                    entry != null && entry.score > userScore
                } + 1
                trySend(Result.success(rank))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }

        leaderboardRef.addValueEventListener(listener)
        awaitClose { leaderboardRef.removeEventListener(listener) }
    }

    /**
     * Updates or creates a user's leaderboard entry in Firebase.
     */
    suspend fun updateUserScore(userId: String, nickname: String, newScore: Int, stage: Int) {
        try {
            val entry = mapOf(
                "nickname" to nickname,
                "score" to newScore,
                "stage" to stage
            )
            leaderboardRef.child(userId).updateChildren(entry).await()
            AppLogger.i(TAG, "Leaderboard updated for $userId: score=$newScore, stage=$stage")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to update leaderboard for $userId", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "LeaderboardRepository"
    }
}
