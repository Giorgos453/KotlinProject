package com.example.myapplication.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.leaderboard.LeaderboardRepository
import com.example.myapplication.data.profile.ProfileRepository
import com.example.myapplication.data.quiz.QuizQuestion
import com.example.myapplication.data.quiz.QuizRepository
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(
    private val repository: QuizRepository,
    private val leaderboardRepository: LeaderboardRepository?,
    private val profileRepository: ProfileRepository?,
    private val userId: String?,
    private val userNickname: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var questions: List<QuizQuestion> = emptyList()

    init {
        startNewSession()
    }

    fun startNewSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.seedIfNeeded()
                questions = repository.getRandomQuestions(QUESTIONS_PER_SESSION)
                if (questions.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "No quiz questions available.") }
                    return@launch
                }
                _uiState.value = QuizUiState(
                    currentQuestion = questions.first(),
                    questionNumber = 1
                )
                AppLogger.i(TAG, "New quiz session started with ${questions.size} questions")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to load quiz questions", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to load questions.") }
            }
        }
    }

    fun selectAnswer(index: Int) {
        val state = _uiState.value
        if (state.selectedAnswer != null || state.currentQuestion == null) return

        val isCorrect = index == state.currentQuestion.correctIndex
        val newScore = if (isCorrect) state.score + POINTS_PER_CORRECT else state.score
        val answered = AnsweredQuestion(
            question = state.currentQuestion,
            selectedIndex = index,
            wasCorrect = isCorrect
        )

        _uiState.update {
            it.copy(
                selectedAnswer = index,
                showCorrectAnswer = true,
                score = newScore,
                answeredQuestions = it.answeredQuestions + answered
            )
        }

        viewModelScope.launch {
            delay(if (isCorrect) CORRECT_DELAY_MS else WRONG_DELAY_MS)
            advanceToNext()
        }
    }

    private fun advanceToNext() {
        val state = _uiState.value
        val nextIndex = state.questionNumber // 1-based, so this is the next 0-based index
        if (nextIndex >= questions.size) {
            _uiState.update { it.copy(isSessionComplete = true, currentQuestion = null, selectedAnswer = null, showCorrectAnswer = false) }
            AppLogger.i(TAG, "Quiz session complete. Score: ${state.score}/${questions.size * POINTS_PER_CORRECT}")
            submitScoreToLeaderboard(state.score)
        } else {
            _uiState.update {
                it.copy(
                    currentQuestion = questions[nextIndex],
                    questionNumber = nextIndex + 1,
                    selectedAnswer = null,
                    showCorrectAnswer = false
                )
            }
        }
    }

    private fun submitScoreToLeaderboard(sessionScore: Int) {
        if (userId == null || userNickname == null) return
        viewModelScope.launch {
            try {
                // Update leaderboard
                val stage = sessionScore / POINTS_PER_CORRECT
                leaderboardRepository?.updateUserScore(userId, userNickname, sessionScore, stage)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to submit score to leaderboard", e)
            }
            try {
                // Update profile (accumulated score + quiz count)
                profileRepository?.addScoreAndIncrementQuiz(userId, sessionScore)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to update profile after quiz", e)
            }
        }
    }

    fun resetSession() {
        _uiState.value = QuizUiState()
        startNewSession()
    }

    class Factory(
        private val repository: QuizRepository,
        private val leaderboardRepository: LeaderboardRepository? = null,
        private val profileRepository: ProfileRepository? = null,
        private val userId: String? = null,
        private val userNickname: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
                return QuizViewModel(repository, leaderboardRepository, profileRepository, userId, userNickname) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "QuizViewModel"
        const val QUESTIONS_PER_SESSION = 3
        const val POINTS_PER_CORRECT = 10
        private const val CORRECT_DELAY_MS = 1500L
        private const val WRONG_DELAY_MS = 2000L
    }
}
