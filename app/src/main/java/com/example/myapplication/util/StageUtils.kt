package com.example.myapplication.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Shared stage utilities used by Profile, Leaderboard, and Quiz screens.
 */
object StageUtils {

    fun calculateStage(totalScore: Int): Int = when {
        totalScore >= 500 -> 5
        totalScore >= 300 -> 4
        totalScore >= 200 -> 3
        totalScore >= 100 -> 2
        totalScore >= 50 -> 1
        else -> 0
    }

    fun getStageName(stage: Int): String = when (stage) {
        0 -> "Beginner"
        1 -> "Amateur"
        2 -> "Advanced"
        3 -> "Expert"
        4 -> "Master"
        5 -> "Legend"
        else -> "Beginner"
    }

    fun getStageIcon(stage: Int): ImageVector = when (stage) {
        0 -> Icons.Default.Person
        else -> Icons.Default.Star
    }

    fun getStageColor(stage: Int): Color = when (stage) {
        0 -> Color(0xFF9E9E9E)
        1 -> Color(0xFF4CAF50)
        2 -> Color(0xFF2196F3)
        3 -> Color(0xFF9C27B0)
        4 -> Color(0xFFFF9800)
        5 -> Color(0xFFFFD700)
        else -> Color(0xFF9E9E9E)
    }
}
