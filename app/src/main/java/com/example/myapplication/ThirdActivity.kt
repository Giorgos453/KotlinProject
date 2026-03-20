package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.app.Activity
import com.example.myapplication.util.AppLogger

/**
 * ThirdActivity: Demonstriert Activity-Management mit finish().
 * Die Activity wird geschlossen, wenn zu einer anderen navigiert wird
 * oder der Zurück-Button betätigt wird.
 */
class ThirdActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)
        AppLogger.i(TAG, "onCreate")

        val btnGoToSecond = findViewById<Button>(R.id.btnGoToSecond)
        val btnBack = findViewById<Button>(R.id.btnBackThird)

        // Navigation zu SecondActivity — aktuelle Activity wird mit finish() geschlossen
        btnGoToSecond.setOnClickListener {
            AppLogger.d(TAG, "Navigating to SecondActivity, closing ThirdActivity")
            startActivity(Intent(this, SecondActivity::class.java))
            finish() // Aktuelle Activity schließen
        }

        // Zurück-Button — Activity mit finish() schließen
        btnBack.setOnClickListener {
            AppLogger.d(TAG, "Back button pressed, finishing ThirdActivity")
            finish()
        }
    }

    companion object {
        private const val TAG = "ThirdActivity"
    }
}
