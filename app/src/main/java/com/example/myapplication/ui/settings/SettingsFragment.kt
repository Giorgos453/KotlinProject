package com.example.myapplication.ui.settings

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.myapplication.R
import com.example.myapplication.data.preferences.PreferenceKeys
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.data.security.ApiKeyManager
import com.example.myapplication.util.AppLogger

/**
 * Settings fragment based on PreferenceFragmentCompat.
 * Reads/writes all values through PreferencesManager (repository layer).
 * Profile + API key management is delegated to the combined ApiKeyFragment.
 */
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferencesManager = PreferencesManager(requireContext())

        preferenceManager.sharedPreferencesName = PreferenceKeys.PREFS_NAME

        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupDynamicColorsVisibility()
        setupProfileApiPreference()
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        updateProfileApiSummary()
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceKeys.KEY_USER_NAME -> {
                AppLogger.i(TAG, "Username changed to: ${preferencesManager.userName}")
            }
            PreferenceKeys.KEY_THEME_MODE -> {
                AppLogger.i(TAG, "Theme changed to: ${preferencesManager.themeMode}")
                requireActivity().recreate()
            }
            PreferenceKeys.KEY_DYNAMIC_COLORS -> {
                AppLogger.i(TAG, "Dynamic colors: ${preferencesManager.dynamicColorsEnabled}")
                requireActivity().recreate()
            }
            PreferenceKeys.KEY_LOCATION_UPDATE_INTERVAL -> {
                AppLogger.i(TAG, "Location interval changed to: ${preferencesManager.locationUpdateInterval}ms")
            }
            PreferenceKeys.KEY_MAP_DEFAULT_ZOOM -> {
                AppLogger.i(TAG, "Map zoom changed to: ${preferencesManager.mapDefaultZoom}")
            }
        }
    }

    private fun setupDynamicColorsVisibility() {
        val dynamicColorsPref = findPreference<SwitchPreferenceCompat>(PreferenceKeys.KEY_DYNAMIC_COLORS)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            dynamicColorsPref?.isVisible = false
        }
    }

    /** Clicking "Profile & API" opens the combined ApiKeyFragment */
    private fun setupProfileApiPreference() {
        val pref = findPreference<Preference>(KEY_MANAGE_PROFILE_API)
        pref?.setOnPreferenceClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, ApiKeyFragment())
                .addToBackStack(null)
                .commit()
            true
        }
        updateProfileApiSummary()
    }

    /** Updates the summary based on stored user and API key status */
    private fun updateProfileApiSummary() {
        val pref = findPreference<Preference>(KEY_MANAGE_PROFILE_API)
        val hasKey = ApiKeyManager.getInstance(requireContext()).hasApiKey()
        val userName = preferencesManager.userName
        pref?.summary = if (hasKey) {
            "$userName — ${getString(R.string.pref_api_key_summary_set)}"
        } else {
            "$userName — ${getString(R.string.pref_api_key_summary_not_set)}"
        }
    }

    companion object {
        private const val TAG = "SettingsFragment"
        private const val KEY_MANAGE_PROFILE_API = "manage_profile_api"
    }
}
