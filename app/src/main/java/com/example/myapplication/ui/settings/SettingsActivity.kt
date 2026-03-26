package com.example.myapplication.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.util.AppLogger
import com.google.android.material.appbar.MaterialToolbar

/**
 * Container-Activity für das SettingsFragment.
 * Nutzt eigenes Layout mit Toolbar + Fragment-Container,
 * damit die Toolbar den Inhalt nicht überdeckt.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        AppLogger.i(TAG, "onCreate")

        // Toolbar als ActionBar setzen und Zurück-Pfeil aktivieren
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        // Fragment in den Container laden (nicht bei Rotation)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    companion object {
        private const val TAG = "SettingsActivity"

        /** Factory-Methode für typsicheren Intent-Aufruf */
        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
