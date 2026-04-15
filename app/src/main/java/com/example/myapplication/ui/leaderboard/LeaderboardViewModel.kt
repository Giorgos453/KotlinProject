package com.example.myapplication.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.leaderboard.LeaderboardRepository
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val repository: LeaderboardRepository,
    private val currentUserId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState(currentUserId = currentUserId))
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            repository.getTopLeaderboard(20).collect { result ->
                result.onSuccess { entries ->
                    _uiState.update { it.copy(entries = entries, isLoading = false, error = null) }
                }.onFailure { e ->
                    AppLogger.e(TAG, "Failed to load leaderboard", e)
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load leaderboard.") }
                }
            }
        }

        // Load user rank separately if user is authenticated
        if (currentUserId != null) {
            viewModelScope.launch {
                repository.getUserRank(currentUserId).collect { result ->
                    result.onSuccess { rank ->
                        _uiState.update { it.copy(userRank = rank) }
                    }
                }
            }
        }
    }

    fun refreshLeaderboard() {
        loadLeaderboard()
    }

    class Factory(
        private val repository: LeaderboardRepository,
        private val currentUserId: String?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
                return LeaderboardViewModel(repository, currentUserId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "LeaderboardViewModel"
    }
}
