package com.example.myapplication.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.myapplication.util.AppLogger

/**
 * Singleton-Manager fuer den verschluesselten API-Key.
 *
 * Sicherheitsentscheidungen:
 * - AES256_GCM MasterKey: Hardware-gestuetzter Schluessel im Android Keystore
 * - AES256_SIV fuer Key-Verschluesselung: Deterministic Encryption verhindert Key-Analyse
 * - AES256_GCM fuer Value-Verschluesselung: Authenticated Encryption schuetzt Integritaet + Vertraulichkeit
 * - Separate Datei "secure_prefs": Sensible Daten isoliert von normalen SharedPreferences
 * - API-Key wird niemals geloggt – nur Statusaenderungen (gespeichert/geloescht)
 */
class ApiKeyManager private constructor(context: Context) {

    private val prefs: SharedPreferences

    init {
        // MasterKey im Android Keystore erzeugen (Hardware-backed wenn verfuegbar)
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // EncryptedSharedPreferences – Keys UND Values werden verschluesselt
        prefs = EncryptedSharedPreferences.create(
            context.applicationContext,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /** Speichert den API-Key verschluesselt. Key wird vorher getrimmt. */
    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
        AppLogger.i(TAG, "API key saved (encrypted)")
    }

    /** Liest den API-Key. Gibt null zurueck wenn kein Key gespeichert ist. */
    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)?.trim()
    }

    /** Prueft ob ein gueltiger (nicht-leerer) API-Key vorhanden ist. */
    fun hasApiKey(): Boolean {
        return getApiKey()?.isNotBlank() == true
    }

    /** Loescht den gespeicherten API-Key. */
    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
        AppLogger.i(TAG, "API key cleared")
    }

    companion object {
        private const val TAG = "ApiKeyManager"
        private const val PREFS_FILE_NAME = "secure_prefs"
        private const val KEY_API_KEY = "api_key"

        /** Mindestlaenge fuer einen gueltigen API-Key */
        const val MIN_KEY_LENGTH = 4

        @Volatile
        private var INSTANCE: ApiKeyManager? = null

        /** Singleton-Zugriff – Thread-safe */
        fun getInstance(context: Context): ApiKeyManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiKeyManager(context.applicationContext).also { INSTANCE = it }
            }

        /**
         * Validiert einen API-Key vor dem Speichern.
         * Gibt eine Fehlermeldung zurueck oder null wenn gueltig.
         */
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

    /** Ergebnis der API-Key-Validierung */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
