package com.example.myapplication.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.airbuddy.TreeStateRepository
import com.example.myapplication.data.airbuddy.scoreToAvatarStage
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.data.profile.ProfileRepository
import com.example.myapplication.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val treeStateRepository: TreeStateRepository?,
    private val userId: String?,
    private val preferencesManager: PreferencesManager,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _selectedAvatarId = MutableStateFlow(preferencesManager.selectedAvatarId)
    val selectedAvatarId: StateFlow<Int> = _selectedAvatarId.asStateFlow()

    private val _customAvatarPath = MutableStateFlow(preferencesManager.customAvatarPath)
    val customAvatarPath: StateFlow<String?> = _customAvatarPath.asStateFlow()

    private val _userName = MutableStateFlow(preferencesManager.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        loadUserProfile()
        observeTreeState()
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

    private fun observeTreeState() {
        if (userId == null || treeStateRepository == null) return
        viewModelScope.launch {
            treeStateRepository.observeTreeState(userId).collect { state ->
                _uiState.update {
                    it.copy(
                        xp = state.currentScore,
                        avatarStage = scoreToAvatarStage(state.currentScore),
                        loginStreak = state.currentLoginStreak,
                        quizzesCompleted = state.totalQuizzesCompleted,
                        parksVisited = state.getVisitedParkIdSet().size
                    )
                }
            }
        }
    }

    fun selectAvatar(id: Int) {
        preferencesManager.selectedAvatarId = id
        preferencesManager.customAvatarPath = null
        _selectedAvatarId.value = id
        _customAvatarPath.value = null
    }

    fun setCustomAvatar(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = appContext.contentResolver.openInputStream(uri) ?: return@launch
                val file = File(appContext.filesDir, "custom_avatar.jpg")
                file.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                val path = file.absolutePath
                preferencesManager.customAvatarPath = path
                preferencesManager.selectedAvatarId = -1
                _customAvatarPath.value = path
                _selectedAvatarId.value = -1
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to save custom avatar", e)
            }
        }
    }

    fun updateUsername(name: String) {
        preferencesManager.userName = name
        _userName.value = name
        val uid = userId ?: return
        viewModelScope.launch {
            profileRepository.updateUsername(uid, name)
        }
    }

    class Factory(
        private val profileRepository: ProfileRepository,
        private val treeStateRepository: TreeStateRepository?,
        private val userId: String?,
        private val preferencesManager: PreferencesManager,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(
                    profileRepository,
                    treeStateRepository,
                    userId,
                    preferencesManager,
                    appContext
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}
