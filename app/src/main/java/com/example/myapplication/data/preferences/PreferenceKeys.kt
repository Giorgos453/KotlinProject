package com.example.myapplication.data.preferences

/**
 * Zentrale Definition aller SharedPreference-Keys und Default-Werte.
 * Keine Magic Strings – alle Zugriffe referenzieren diese Konstanten.
 */
object PreferenceKeys {

    // SharedPreferences-Dateiname
    const val PREFS_NAME = "app_prefs"

    // --- Profil ---
    const val KEY_USER_NAME = "user_id"
    const val DEFAULT_USER_NAME = "User"

    // --- Location ---
    const val KEY_LOCATION_RECORDING_ENABLED = "location_recording_enabled"
    const val DEFAULT_LOCATION_RECORDING_ENABLED = false

    const val KEY_LOCATION_UPDATE_INTERVAL = "location_update_interval"
    const val DEFAULT_LOCATION_UPDATE_INTERVAL = "5000" // Millisekunden

    // --- Map ---
    const val KEY_MAP_DEFAULT_ZOOM = "map_default_zoom"
    const val DEFAULT_MAP_DEFAULT_ZOOM = "15.0"

    // --- Darstellung ---
    const val KEY_THEME_MODE = "theme_mode"
    const val DEFAULT_THEME_MODE = "system" // system, light, dark

    const val KEY_DYNAMIC_COLORS = "dynamic_colors"
    const val DEFAULT_DYNAMIC_COLORS = false
}
