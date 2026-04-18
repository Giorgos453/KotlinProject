package com.example.myapplication.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.myapplication.util.AppLogger

/**
 * Singleton manager for the encrypted API key.
 * Uses AES256_GCM (hardware-backed keystore) + EncryptedSharedPreferences so both
 * key names and values are encrypted at rest. The API key is never logged.
 */
class ApiKeyManager private constructor(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
        AppLogger.i(TAG, "API key saved (encrypted)")
    }

    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)?.trim()
    }

    fun hasApiKey(): Boolean {
        return getApiKey()?.isNotBlank() == true
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
        AppLogger.i(TAG, "API key cleared")
    }

    companion object {
        private const val TAG = "ApiKeyManager"
        private const val PREFS_FILE_NAME = "secure_prefs"
        private const val KEY_API_KEY = "api_key"

        const val MIN_KEY_LENGTH = 4

        @Volatile
        private var INSTANCE: ApiKeyManager? = null

        fun getInstance(context: Context): ApiKeyManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiKeyManager(context.applicationContext).also { INSTANCE = it }
            }

        fun validateApiKey(key: String): ValidationResult {
            val trimmed = key.trim()
            return when {
                trimmed.isBlank() -> ValidationResult.Error("API key cannot be empty")
                trimmed.length < MIN_KEY_LENGTH -> ValidationResult.Error(
                    "API key must be at least $MIN_KEY_LENGTH characters"
                )
                trimmed.contains(" ") -> ValidationResult.Error("API key must not contain spaces")
                else -> ValidationResult.Valid
            }
        }
    }

    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
