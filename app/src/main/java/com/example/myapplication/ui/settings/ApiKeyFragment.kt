package com.example.myapplication.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.data.security.ApiKeyManager
import com.example.myapplication.util.AppLogger
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Fragment fuer die kombinierte Verwaltung von User Identifier und API-Key.
 *
 * - userIdentifier: unverschluesselt in normalen SharedPreferences (PreferencesManager)
 * - apiKey: maskiert angezeigt, verschluesselt gespeichert (ApiKeyManager / EncryptedSharedPreferences)
 *
 * Ein einzelner "Save"-Button speichert beide Werte gleichzeitig.
 * Nach dem Speichern: Snackbar-Bestaetigung, API-Key-Feld wieder maskiert.
 *
 * SharedPreferences-Pfad im Device Explorer:
 * /data/data/es.upm.btb.helloworldkt/shared_prefs/
 */
class ApiKeyFragment : Fragment() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var apiKeyManager: ApiKeyManager

    private lateinit var tvKeyStatus: TextView
    private lateinit var tilUserIdentifier: TextInputLayout
    private lateinit var etUserIdentifier: TextInputEditText
    private lateinit var tilApiKey: TextInputLayout
    private lateinit var etApiKey: TextInputEditText
    private lateinit var btnSave: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_api_key, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        apiKeyManager = ApiKeyManager.getInstance(requireContext())

        setupViews(view)
        loadCurrentValues()
        updateStatusDisplay()
        setupSaveButton()
    }

    private fun setupViews(view: View) {
        tvKeyStatus = view.findViewById(R.id.tvKeyStatus)
        tilUserIdentifier = view.findViewById(R.id.tilUserIdentifier)
        etUserIdentifier = view.findViewById(R.id.etUserIdentifier)
        tilApiKey = view.findViewById(R.id.tilApiKey)
        etApiKey = view.findViewById(R.id.etApiKey)
        btnSave = view.findViewById(R.id.btnSave)
    }

    /** Laedt die aktuell gespeicherten Werte in die Felder */
    private fun loadCurrentValues() {
        // User Identifier aus normalen SharedPreferences laden
        val currentName = preferencesManager.userName
        if (currentName != "User") {
            etUserIdentifier.setText(currentName)
        }

        // API-Key: Feld leer lassen, nur Placeholder anzeigen wenn Key vorhanden
        if (apiKeyManager.hasApiKey()) {
            tilApiKey.placeholderText = getString(R.string.api_key_placeholder)
        }
    }

    /** Zeigt den aktuellen API-Key-Status an */
    private fun updateStatusDisplay() {
        if (apiKeyManager.hasApiKey()) {
            tvKeyStatus.text = getString(R.string.api_key_status_saved)
            tvKeyStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            tvKeyStatus.text = getString(R.string.api_key_status_not_set)
            tvKeyStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_light))
        }
    }

    /** Ein Save-Button speichert beide Werte gleichzeitig */
    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            var hasError = false

            // User Identifier validieren
            val userName = etUserIdentifier.text?.toString()?.trim()
            if (userName.isNullOrBlank()) {
                tilUserIdentifier.error = getString(R.string.pref_user_name_empty_error)
                hasError = true
            } else {
                tilUserIdentifier.error = null
            }

            // API-Key validieren (nur wenn ein neuer eingegeben wurde)
            val apiKeyInput = etApiKey.text?.toString()?.trim()
            if (!apiKeyInput.isNullOrEmpty()) {
                val validation = ApiKeyManager.validateApiKey(apiKeyInput)
                if (validation is ApiKeyManager.ValidationResult.Error) {
                    tilApiKey.error = validation.message
                    hasError = true
                } else {
                    tilApiKey.error = null
                }
            }

            if (hasError) return@setOnClickListener

            // Beide Werte speichern
            if (!userName.isNullOrBlank()) {
                preferencesManager.userName = userName
                AppLogger.i(TAG, "User identifier saved: $userName")
            }

            if (!apiKeyInput.isNullOrEmpty()) {
                apiKeyManager.saveApiKey(apiKeyInput)
                // Feld leeren und maskieren nach Speichern
                etApiKey.text?.clear()
                etApiKey.clearFocus()
                tilApiKey.placeholderText = getString(R.string.api_key_placeholder)
            }

            updateStatusDisplay()

            // Snackbar-Bestaetigung
            view?.let {
                Snackbar.make(it, R.string.settings_saved_success, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "ApiKeyFragment"
    }
}
