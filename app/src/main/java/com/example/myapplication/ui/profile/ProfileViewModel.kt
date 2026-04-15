package com.example.myapplication.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.profile.ProfileRepository
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val userId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false, error = "Not signed in.") }
            return
        }

        viewModelScope.launch {
            profileRepository.observeUserProfile(userId)
                .catch { e ->
                    AppLogger.e(TAG, "Failed to observe profile", e)
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { profile ->
                    _uiState.update { it.copy(profile = profile, isLoading = false, error = null) }
                }
        }
    }

    class Factory(
        private val profileRepository: ProfileRepository,
        private val userId: String?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(profileRepository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
