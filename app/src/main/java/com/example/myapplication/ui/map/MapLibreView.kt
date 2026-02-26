package com.example.myapplication.ui.map

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

private const val TAG = "MapLibreView"

/**
 * Wraps a MapLibre [MapView] inside a Composable with correct lifecycle management.
 *
 * @param styleUrl   MapLibre-compatible style URL.
 * @param onMapReady Invoked once the map and style are fully loaded.
 */
@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    styleUrl: String,
    onMapReady: (mapView: MapView, map: MapLibreMap, style: Style) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                addOnDidFailLoadingMapListener { errorMessage ->
                    Log.e(TAG, "Map failed to load: $errorMessage")
                }

                getMapAsync { map ->
                    Log.d(TAG, "Loading style: $styleUrl")
                    map.setStyle(styleUrl) { style ->
                        Log.d(TAG, "Style loaded successfully")
                        onMapReady(this, map, style)
                    }
                }
            }
        },
        modifier = modifier
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE  -> { /* already called via onCreate(null) */ }
                Lifecycle.Event.ON_START   -> mapView.onStart()
                Lifecycle.Event.ON_RESUME  -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE   -> mapView.onPause()
                Lifecycle.Event.ON_STOP    -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }
}
