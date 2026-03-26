package com.example.myapplication.ui.map

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.data.map.CampusMarker
import com.example.myapplication.util.AppLogger
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

private const val TAG = "MapScreen"
private const val STYLE_URL =
    "https://api.maptiler.com/maps/streets-v2/style.json?key=pM3HjlbzLivwuGW5YOeG"

// Fallback camera position used until the first GPS fix arrives
private const val DEFAULT_LAT = 40.38950
private const val DEFAULT_LNG = -3.62790
private const val MAP_ZOOM = 15.0

// Campus markers and route are only rendered at or above this zoom level
private const val MIN_ZOOM_CAMPUS_MARKERS = 13.0

private fun vectorToBitmap(context: Context, @DrawableRes resId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context, resId)
        ?: throw IllegalArgumentException("Drawable resource $resId not found")
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Composable
fun MapScreen(
    viewModel: MapViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        AppLogger.i(TAG, "Permission callback: granted=$granted")
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        if (!uiState.permissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        val userMarkerHolder = remember { arrayOfNulls<Marker>(1) }
        val campusMarkerLookup = remember { mutableMapOf<Long, CampusMarker>() }
        val campusMarkerObjects = remember { mutableListOf<Marker>() }
        val polylineHolder = remember { arrayOfNulls<Polyline>(1) }
        val mapViewHolder = remember { arrayOfNulls<MapView>(1) }

        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            styleUrl = STYLE_URL,
            onMapReady = { mapView, map, _ ->
                AppLogger.d(TAG, "Map ready")
                mapViewHolder[0] = mapView

                val iconFactory = IconFactory.getInstance(context)
                val userLat = uiState.userLatitude
                val userLng = uiState.userLongitude

                if (userLat != null && userLng != null) {
                    val userIcon = iconFactory.fromBitmap(
                        vectorToBitmap(context, R.drawable.ic_marker_user)
                    )
                    userMarkerHolder[0] = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(userLat, userLng))
                            .title("My current location")
                            .icon(userIcon)
                    )
                }

                val campusIcon = iconFactory.fromBitmap(
                    vectorToBitmap(context, R.drawable.ic_marker_campus)
                )

                fun showCampusElements() {
                    if (campusMarkerObjects.isNotEmpty()) return
                    for (cm in uiState.campusMarkers) {
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(LatLng(cm.latitude, cm.longitude))
                                .title(cm.title)
                                .snippet(cm.description)
                                .icon(campusIcon)
                        )
                        campusMarkerObjects.add(marker)
                        campusMarkerLookup[marker.id] = cm
                    }
                    val routePoints = uiState.campusMarkers.map { LatLng(it.latitude, it.longitude) }
                    if (routePoints.size >= 2) {
                        polylineHolder[0] = map.addPolyline(
                            PolylineOptions()
                                .addAll(routePoints)
                                .color(Color.parseColor("#1565C0"))
                                .width(4f)
                        )
                    }
                    AppLogger.d(TAG, "Campus elements shown (zoom=${map.cameraPosition.zoom})")
                }

                fun hideCampusElements() {
                    if (campusMarkerObjects.isEmpty()) return
                    campusMarkerObjects.forEach { campusMarkerLookup.remove(it.id); it.remove() }
                    campusMarkerObjects.clear()
                    polylineHolder[0]?.remove()
                    polylineHolder[0] = null
                    AppLogger.d(TAG, "Campus elements hidden (zoom=${map.cameraPosition.zoom})")
                }

                // Show campus markers and route only when zoomed in sufficiently
                map.addOnCameraIdleListener {
                    if (map.cameraPosition.zoom >= MIN_ZOOM_CAMPUS_MARKERS) {
                        showCampusElements()
                    } else {
                        hideCampusElements()
                    }
                }

                map.setOnMarkerClickListener { marker ->
                    val campusMarker = campusMarkerLookup[marker.id]
                    if (campusMarker != null) {
                        viewModel.onMarkerSelected(campusMarker)
                        true
                    } else {
                        false
                    }
                }

                val cameraTarget = if (userLat != null && userLng != null) {
                    LatLng(userLat, userLng)
                } else {
                    LatLng(DEFAULT_LAT, DEFAULT_LNG)
                }
                map.cameraPosition = CameraPosition.Builder()
                    .target(cameraTarget)
                    .zoom(MAP_ZOOM)
                    .build()
            }
        )

        // Re-center map and update user marker on each location update
        LaunchedEffect(uiState.userLatitude, uiState.userLongitude) {
            val lat = uiState.userLatitude ?: return@LaunchedEffect
            val lng = uiState.userLongitude ?: return@LaunchedEffect
            val mapView = mapViewHolder[0] ?: return@LaunchedEffect

            mapView.getMapAsync { map ->
                userMarkerHolder[0]?.remove()

                val iconFactory = IconFactory.getInstance(context)
                val userIcon = iconFactory.fromBitmap(
                    vectorToBitmap(context, R.drawable.ic_marker_user)
                )
                userMarkerHolder[0] = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lng))
                        .title("My current location")
                        .icon(userIcon)
                )

                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(lat, lng))
                    .zoom(MAP_ZOOM)
                    .build()
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            )
        }

        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        uiState.userAddress?.let { address ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Your Location", style = MaterialTheme.typography.labelMedium)
                    Text(address, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        uiState.selectedMarker?.let { marker ->
            MarkerInfoCard(
                marker = marker,
                address = uiState.selectedMarkerAddress,
                onDismiss = { viewModel.dismissMarkerInfo() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun MarkerInfoCard(
    marker: CampusMarker,
    address: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = marker.title, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = marker.description, style = MaterialTheme.typography.bodyMedium)

            address?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}
