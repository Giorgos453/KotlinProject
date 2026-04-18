package com.example.myapplication.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.leaderboard.LeaderboardRepository
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.Job
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

    private var leaderboardJob: Job? = null
    private var rankJob: Job? = null

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        leaderboardJob?.cancel()
        rankJob?.cancel()

        _uiState.update { it.copy(isLoading = true, error = null) }

        leaderboardJob = viewModelScope.launch {
            repository.getTopLeaderboard(20, currentUserId).collect { result ->
                result.onSuccess { entries ->
                    val currentEntry = entries.firstOrNull { it.userId == currentUserId }
                    _uiState.update {
                        it.copy(
                            entries = entries,
                            currentUserEntry = currentEntry ?: it.currentUserEntry,
                            isLoading = false,
                            error = null
                        )
                    }
                    if (currentEntry == null && currentUserId != null) {
                        loadCurrentUserEntry()
                    }
                }.onFailure { e ->
                    AppLogger.e(TAG, "Failed to load leaderboard", e)
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load leaderboard.") }
                }
            }
        }

        if (currentUserId != null) {
            rankJob = viewModelScope.launch {
                repository.getUserRank(currentUserId).collect { result ->
                    result.onSuccess { rank ->
                        _uiState.update { it.copy(userRank = rank) }
                    }
                }
            }
        }
    }

    private fun loadCurrentUserEntry() {
        if (currentUserId == null) return
        viewModelScope.launch {
            val entry = repository.getCurrentUserEntry(currentUserId)
            if (entry != null) {
                _uiState.update { it.copy(currentUserEntry = entry) }
            }
        }
    }

    fun refreshLeaderboard() {
        loadLeaderboard()
    }

    override fun onCleared() {
        super.onCleared()
        leaderboardJob?.cancel()
        rankJob?.cancel()
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
