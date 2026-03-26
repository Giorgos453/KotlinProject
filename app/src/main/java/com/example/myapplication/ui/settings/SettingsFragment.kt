package com.example.myapplication.ui.settings

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.myapplication.R
import com.example.myapplication.data.preferences.PreferenceKeys
import com.example.myapplication.data.preferences.PreferencesManager
import com.example.myapplication.util.AppLogger

/**
 * Settings-Fragment basierend auf PreferenceFragmentCompat.
 * Liest/schreibt alle Werte über den PreferencesManager (Repository-Schicht).
 * Die preferences.xml definiert die UI-Struktur.
 */
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var preferencesManager: PreferencesManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // PreferencesManager nutzt denselben SharedPreferences-Dateinamen
        preferencesManager = PreferencesManager(requireContext())

        // PreferenceManager auf unseren SharedPreferences-Namen setzen,
        // damit preferences.xml in dieselbe Datei schreibt
        preferenceManager.sharedPreferencesName = PreferenceKeys.PREFS_NAME

        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupUserNameValidation()
        setupDynamicColorsVisibility()
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Reagiert auf Änderungen an Preferences und loggt diese.
     * Hier können auch sofortige Aktionen ausgelöst werden (z. B. Theme-Wechsel).
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceKeys.KEY_THEME_MODE -> {
                AppLogger.i(TAG, "Theme changed to: ${preferencesManager.themeMode}")
                // Activity neu starten, damit Theme-Änderung sichtbar wird
                requireActivity().recreate()
            }
            PreferenceKeys.KEY_DYNAMIC_COLORS -> {
                AppLogger.i(TAG, "Dynamic colors: ${preferencesManager.dynamicColorsEnabled}")
                requireActivity().recreate()
            }
            PreferenceKeys.KEY_USER_NAME -> {
                AppLogger.i(TAG, "User name changed to: ${preferencesManager.userName}")
            }
            PreferenceKeys.KEY_LOCATION_UPDATE_INTERVAL -> {
                AppLogger.i(TAG, "Location interval changed to: ${preferencesManager.locationUpdateInterval}ms")
            }
            PreferenceKeys.KEY_MAP_DEFAULT_ZOOM -> {
                AppLogger.i(TAG, "Map zoom changed to: ${preferencesManager.mapDefaultZoom}")
            }
        }
    }

    /**
     * Validiert die Benutzername-Eingabe: leeres Feld wird abgelehnt.
     */
    private fun setupUserNameValidation() {
        val userNamePref = findPreference<EditTextPreference>(PreferenceKeys.KEY_USER_NAME)
        userNamePref?.setOnPreferenceChangeListener { _, newValue ->
            val name = (newValue as? String)?.trim()
            if (name.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    R.string.pref_user_name_empty_error,
                    Toast.LENGTH_SHORT
                ).show()
                false // Änderung ablehnen
            } else {
                true // Änderung akzeptieren
            }
        }
    }

    /**
     * Dynamic Colors nur auf Android 12+ verfügbar.
     * Auf älteren Geräten wird die Option ausgeblendet.
     */
    private fun setupDynamicColorsVisibility() {
        val dynamicColorsPref = findPreference<SwitchPreferenceCompat>(PreferenceKeys.KEY_DYNAMIC_COLORS)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            dynamicColorsPref?.isVisible = false
        }
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}
