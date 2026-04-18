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
 * Fragment for combined management of user identifier and API key.
 *
 * - userIdentifier: stored plaintext in regular SharedPreferences (PreferencesManager)
 * - apiKey: shown masked, stored encrypted (ApiKeyManager / EncryptedSharedPreferences)
 *
 * A single "Save" button persists both values at once.
 * After saving: Snackbar confirmation; API key field is re-masked.
 *
 * SharedPreferences path in Device Explorer:
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

    /** Loads the currently stored values into the fields */
    private fun loadCurrentValues() {
        // load user identifier from regular SharedPreferences
        val currentName = preferencesManager.userName
        if (currentName != "User") {
            etUserIdentifier.setText(currentName)
        }

        // API key: leave field blank, only show placeholder when a key is stored
        if (apiKeyManager.hasApiKey()) {
            tilApiKey.placeholderText = getString(R.string.api_key_placeholder)
        }
    }

    /** Displays the current API key status */
    private fun updateStatusDisplay() {
        if (apiKeyManager.hasApiKey()) {
            tvKeyStatus.text = getString(R.string.api_key_status_saved)
            tvKeyStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        } else {
            tvKeyStatus.text = getString(R.string.api_key_status_not_set)
            tvKeyStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_light))
        }
    }

    /** Single save button persists both values at once */
    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            var hasError = false

            // validate user identifier
            val userName = etUserIdentifier.text?.toString()?.trim()
            if (userName.isNullOrBlank()) {
                tilUserIdentifier.error = getString(R.string.pref_user_name_empty_error)
                hasError = true
            } else {
                tilUserIdentifier.error = null
            }

            // validate API key (only when a new one is entered)
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

            // save both values
            if (!userName.isNullOrBlank()) {
                preferencesManager.userName = userName
                AppLogger.i(TAG, "User identifier saved: $userName")
            }

            if (!apiKeyInput.isNullOrEmpty()) {
                apiKeyManager.saveApiKey(apiKeyInput)
                // clear and mask field after saving
                etApiKey.text?.clear()
                etApiKey.clearFocus()
                tilApiKey.placeholderText = getString(R.string.api_key_placeholder)
            }

            updateStatusDisplay()

            // Snackbar confirmation
            view?.let {
                Snackbar.make(it, R.string.settings_saved_success, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "ApiKeyFragment"
    }
}
