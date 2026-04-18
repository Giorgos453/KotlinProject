package com.example.myapplication.data.airbuddy

import com.example.myapplication.data.airbuddy.model.TreeState
import com.example.myapplication.data.database.dao.TreeStateDao
import com.example.myapplication.util.AppLogger
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TreeStateRepository(
    private val database: FirebaseDatabase,
    private val treeStateDao: TreeStateDao
) {
    private val usersRef = database.reference.child("users")

    suspend fun getTreeState(userId: String): TreeState {
        return try {
            val snapshot = usersRef.child(userId).child("treeState").get().await()
            val remote = snapshotToTreeState(snapshot, userId)
            if (remote != null) {
                treeStateDao.upsert(remote)
                remote
            } else {
                treeStateDao.getTreeState(userId) ?: TreeState(userId = userId)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to fetch tree state from Firebase", e)
            treeStateDao.getTreeState(userId) ?: TreeState(userId = userId)
        }
    }

    fun observeTreeState(userId: String): Flow<TreeState> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val state = snapshotToTreeState(snapshot, userId) ?: TreeState(userId = userId)
                trySend(state)
            }
            override fun onCancelled(error: DatabaseError) {
                AppLogger.e(TAG, "Tree state observation cancelled", error.toException())
                close(error.toException())
            }
        }
        usersRef.child(userId).child("treeState").addValueEventListener(listener)
        awaitClose { usersRef.child(userId).child("treeState").removeEventListener(listener) }
    }

    suspend fun saveTreeState(state: TreeState) {
        treeStateDao.upsert(state)
        try {
            // Single atomic updateChildren so the nested treeState and the
            // top-level leaderboard mirror (`xp`, `avatarStage`, …) can never
            // drift apart due to a partial network failure.
            val updates = mapOf(
                "treeState/currentScore" to state.currentScore,
                "treeState/lastLoginDate" to state.lastLoginDate,
                "treeState/currentLoginStreak" to state.currentLoginStreak,
                "treeState/longestLoginStreak" to state.longestLoginStreak,
                "treeState/totalQuizzesCompleted" to state.totalQuizzesCompleted,
                "treeState/lastQuizDate" to state.lastQuizDate,
                "treeState/visitedParkIds" to state.visitedParkIds,
                "treeState/lastScoreUpdateDate" to state.lastScoreUpdateDate,
                "treeState/reachedMilestones" to state.reachedMilestones,
                "treeState/avatarStage" to state.avatarStage,
                "xp" to state.currentScore,
                "avatarStage" to state.avatarStage,
                "currentLoginStreak" to state.currentLoginStreak,
                "longestLoginStreak" to state.longestLoginStreak,
                "totalQuizzesCompleted" to state.totalQuizzesCompleted,
                "visitedParkIds" to state.visitedParkIds,
                "lastLoginDate" to state.lastLoginDate,
                "lastQuizDate" to state.lastQuizDate
            )
            usersRef.child(state.userId).updateChildren(updates).await()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save tree state to Firebase", e)
        }
    }

    suspend fun ensureTreeStateExists(userId: String) {
        val existing = getTreeState(userId)
        if (existing.lastLoginDate == 0L) {
            saveTreeState(TreeState(userId = userId))
        }
    }

    // Re-mirrors the current TreeState to users/{uid}. Repairs a stale top-level
    // `xp` left over from an older code path that wrote per-session scores.
    suspend fun syncLeaderboardMirror(userId: String) {
        val state = getTreeState(userId)
        saveTreeState(state)
    }

    private fun snapshotToTreeState(snapshot: DataSnapshot, userId: String): TreeState? {
        if (!snapshot.exists()) return null
        return TreeState(
            userId = userId,
            currentScore = snapshot.child("currentScore").getValue(Int::class.java) ?: 50,
            lastLoginDate = snapshot.child("lastLoginDate").getValue(Long::class.java) ?: 0L,
            currentLoginStreak = snapshot.child("currentLoginStreak").getValue(Int::class.java) ?: 0,
            longestLoginStreak = snapshot.child("longestLoginStreak").getValue(Int::class.java) ?: 0,
            totalQuizzesCompleted = snapshot.child("totalQuizzesCompleted").getValue(Int::class.java) ?: 0,
            lastQuizDate = snapshot.child("lastQuizDate").getValue(Long::class.java) ?: 0L,
            visitedParkIds = snapshot.child("visitedParkIds").getValue(String::class.java) ?: "",
            lastScoreUpdateDate = snapshot.child("lastScoreUpdateDate").getValue(Long::class.java) ?: 0L,
            reachedMilestones = snapshot.child("reachedMilestones").getValue(String::class.java) ?: "",
            avatarStage = snapshot.child("avatarStage").getValue(String::class.java) ?: "SPROUT"
        )
    }

    companion object {
        private const val TAG = "TreeStateRepo"
    }
}
