package com.example.myapplication.ui.quiz

import com.example.myapplication.data.quiz.QuizQuestion

/**
 * UI state for the quiz screen.
 */
data class QuizUiState(
    val currentQuestion: QuizQuestion? = null,
    val questionNumber: Int = 1,
    val score: Int = 0,
    val selectedAnswer: Int? = null,
    val showCorrectAnswer: Boolean = false,
    val isSessionComplete: Boolean = false,
    val answeredQuestions: List<AnsweredQuestion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val treePointsAwarded: Int? = null,
    val treeScoreMessage: String? = null
)

/**
 * Tracks a question along with the user's selected answer for the summary.
 */
data class AnsweredQuestion(
    val question: QuizQuestion,
    val selectedIndex: Int,
    val wasCorrect: Boolean
)
