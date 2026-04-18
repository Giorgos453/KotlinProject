package com.example.myapplication.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.security.ApiKeyManager
import com.example.myapplication.data.security.ApiKeyManager.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ApiKeyUiState(
    val hasKey: Boolean = false,
    val validationError: String? = null,
    val successMessage: String? = null
)

class ApiKeyViewModel(
    private val apiKeyManager: ApiKeyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiKeyUiState(hasKey = apiKeyManager.hasApiKey()))
    val uiState: StateFlow<ApiKeyUiState> = _uiState.asStateFlow()

    fun saveApiKey(key: String): Boolean {
        val result = ApiKeyManager.validateApiKey(key)
        return when (result) {
            is ValidationResult.Valid -> {
                apiKeyManager.saveApiKey(key.trim())
                _uiState.value = ApiKeyUiState(
                    hasKey = true,
                    validationError = null,
                    successMessage = "API key saved successfully"
                )
                true
            }
            is ValidationResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    validationError = result.message,
                    successMessage = null
                )
                false
            }
        }
    }

    fun clearApiKey() {
        apiKeyManager.clearApiKey()
        _uiState.value = ApiKeyUiState(
            hasKey = false,
            validationError = null,
            successMessage = "API key deleted"
        )
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearValidationError() {
        _uiState.value = _uiState.value.copy(validationError = null)
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ApiKeyViewModel::class.java)) {
                return ApiKeyViewModel(ApiKeyManager.getInstance(context.applicationContext)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
