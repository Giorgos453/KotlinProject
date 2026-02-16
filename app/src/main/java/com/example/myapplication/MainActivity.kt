package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.location.LocationRepository
import com.example.myapplication.navigation.AppNavHost
import com.example.myapplication.ui.location.LocationViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.util.AppLogger
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private lateinit var locationViewModelFactory: LocationViewModel.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i(TAG, "onCreate")

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRepository = LocationRepository(fusedLocationClient)
        locationViewModelFactory = LocationViewModel.Factory(locationRepository)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        locationViewModelFactory = locationViewModelFactory
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppLogger.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        AppLogger.i(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        AppLogger.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        AppLogger.i(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.i(TAG, "onDestroy")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
