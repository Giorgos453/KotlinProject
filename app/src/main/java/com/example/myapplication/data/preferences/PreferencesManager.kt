package com.example.myapplication.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data.preferences.PreferenceKeys.DEFAULT_DYNAMIC_COLORS
import com.example.myapplication.data.preferences.PreferenceKeys.DEFAULT_LOCATION_RECORDING_ENABLED
import com.example.myapplication.data.preferences.PreferenceKeys.DEFAULT_LOCATION_UPDATE_INTERVAL
import com.example.myapplication.data.preferences.PreferenceKeys.DEFAULT_MAP_DEFAULT_ZOOM
import com.example.myapplication.data.preferences.PreferenceKeys.DEFAULT_THEME_MODE
import com.example.myapplication.data.preferences.PreferenceKeys.DEFAULT_USER_NAME
import com.example.myapplication.data.preferences.PreferenceKeys.KEY_DYNAMIC_COLORS
import com.example.myapplication.data.preferences.PreferenceKeys.KEY_LOCATION_RECORDING_ENABLED
import com.example.myapplication.data.preferences.PreferenceKeys.KEY_LOCATION_UPDATE_INTERVAL
import com.example.myapplication.data.preferences.PreferenceKeys.KEY_MAP_DEFAULT_ZOOM
import com.example.myapplication.data.preferences.PreferenceKeys.KEY_THEME_MODE
import com.example.myapplication.data.preferences.PreferenceKeys.KEY_USER_NAME
import com.example.myapplication.data.preferences.PreferenceKeys.PREFS_NAME

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, DEFAULT_USER_NAME) ?: DEFAULT_USER_NAME
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    // --- Location ---

    var locationRecordingEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCATION_RECORDING_ENABLED, DEFAULT_LOCATION_RECORDING_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_LOCATION_RECORDING_ENABLED, value).apply()

    var locationUpdateInterval: Long
        get() = (prefs.getString(KEY_LOCATION_UPDATE_INTERVAL, DEFAULT_LOCATION_UPDATE_INTERVAL)
            ?: DEFAULT_LOCATION_UPDATE_INTERVAL).toLong()
        set(value) = prefs.edit().putString(KEY_LOCATION_UPDATE_INTERVAL, value.toString()).apply()

    // --- Map ---

    var mapDefaultZoom: Double
        get() = (prefs.getString(KEY_MAP_DEFAULT_ZOOM, DEFAULT_MAP_DEFAULT_ZOOM)
            ?: DEFAULT_MAP_DEFAULT_ZOOM).toDouble()
        set(value) = prefs.edit().putString(KEY_MAP_DEFAULT_ZOOM, value.toString()).apply()

    // --- Appearance ---

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, DEFAULT_THEME_MODE) ?: DEFAULT_THEME_MODE
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var dynamicColorsEnabled: Boolean
        get() = prefs.getBoolean(KEY_DYNAMIC_COLORS, DEFAULT_DYNAMIC_COLORS)
        set(value) = prefs.edit().putBoolean(KEY_DYNAMIC_COLORS, value).apply()

    var selectedAvatarId: Int
        get() = prefs.getInt("selected_avatar_id", 0)
        set(value) = prefs.edit().putInt("selected_avatar_id", value).apply()

    var customAvatarPath: String?
        get() = prefs.getString("custom_avatar_path", null)
        set(value) = prefs.edit().putString("custom_avatar_path", value).apply()

    fun hasUserName(): Boolean {
        val name = prefs.getString(KEY_USER_NAME, null)
        return !name.isNullOrBlank()
    }

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
