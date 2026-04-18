package com.example.myapplication.ui.map

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.data.airbuddy.MadridPark
import com.example.myapplication.data.database.entity.CampusMarkerEntity
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

private const val DEFAULT_LAT = 40.4165
private const val DEFAULT_LNG = -3.7026
private const val MAP_ZOOM = 15.0
private const val MIN_ZOOM_CAMPUS_MARKERS = 13.0

private val parkAccent = ComposeColor(0xFF2E7D32)
private val parkLightBg = ComposeColor(0xFFE8F5E9)
private val parkPrimary = ComposeColor(0xFF2E4A32)
private val parkMuted = ComposeColor(0xFF8B9590)

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

/** Builds a small bitmap that draws an emoji centered on a circle. */
private fun emojiToBitmap(emoji: String, visited: Boolean): Bitmap {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    bgPaint.color = if (visited) Color.parseColor("#2E7D32") else Color.parseColor("#E8F5E9")
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, bgPaint)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    borderPaint.style = Paint.Style.STROKE
    borderPaint.strokeWidth = 4f
    borderPaint.color = Color.parseColor("#2E7D32")
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, borderPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    textPaint.textSize = 48f
    textPaint.textAlign = Paint.Align.CENTER
    textPaint.typeface = Typeface.DEFAULT
    val fontMetrics = textPaint.fontMetrics
    val textY = size / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(emoji, size / 2f, textY, textPaint)

    if (visited) {
        val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        checkPaint.textSize = 26f
        checkPaint.textAlign = Paint.Align.CENTER
        checkPaint.color = Color.WHITE
        checkPaint.typeface = Typeface.DEFAULT_BOLD
        canvas.drawText("✓", size - 18f, size - 14f, checkPaint)
    }

    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
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
        val campusMarkerLookup = remember { mutableMapOf<Long, CampusMarkerEntity>() }
        val campusMarkerObjects = remember { mutableListOf<Marker>() }
        val parkMarkerLookup = remember { mutableMapOf<Long, MadridPark>() }
        val parkMarkerObjects = remember { mutableMapOf<String, Marker>() }
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

                // Add park markers (always visible)
                fun addParkMarkers() {
                    for (park in uiState.parks) {
                        val visited = park.id in uiState.visitedParkIds
                        val icon = iconFactory.fromBitmap(emojiToBitmap("\uD83C\uDF33", visited))
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(LatLng(park.latitude, park.longitude))
                                .title(park.name)
                                .icon(icon)
                        )
                        parkMarkerLookup[marker.id] = park
                        parkMarkerObjects[park.id] = marker
                    }
                }
                addParkMarkers()

                map.addOnCameraIdleListener {
                    if (map.cameraPosition.zoom >= MIN_ZOOM_CAMPUS_MARKERS) {
                        showCampusElements()
                    } else {
                        hideCampusElements()
                    }
                }

                map.setOnMarkerClickListener { marker ->
                    val campusMarker = campusMarkerLookup[marker.id]
                    val park = parkMarkerLookup[marker.id]
                    when {
                        park != null -> {
                            viewModel.onParkSelected(park)
                            true
                        }
                        campusMarker != null -> {
                            viewModel.onMarkerSelected(campusMarker)
                            true
                        }
                        else -> false
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

        // Refresh park marker icons when visited set changes
        LaunchedEffect(uiState.visitedParkIds) {
            val mapView = mapViewHolder[0] ?: return@LaunchedEffect
            mapView.getMapAsync { map ->
                val iconFactory = IconFactory.getInstance(context)
                for (park in uiState.parks) {
                    val visited = park.id in uiState.visitedParkIds
                    val existing = parkMarkerObjects[park.id]
                    if (existing != null) {
                        existing.remove()
                        parkMarkerLookup.remove(existing.id)
                    }
                    val icon = iconFactory.fromBitmap(emojiToBitmap("\uD83C\uDF33", visited))
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(park.latitude, park.longitude))
                            .title(park.name)
                            .icon(icon)
                    )
                    parkMarkerLookup[marker.id] = park
                    parkMarkerObjects[park.id] = marker
                }
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
            )
        }

        if (uiState.usingFallback) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Text(
                    text = "Location not available \u2013 showing Madrid",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
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

        uiState.selectedPark?.let { park ->
            val visited = park.id in uiState.visitedParkIds
            ParkInfoSheet(
                park = park,
                visited = visited,
                checkInResult = uiState.parkCheckInResult,
                onCheckIn = { viewModel.checkInToPark(park) },
                onDismiss = { viewModel.dismissParkInfo() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParkInfoSheet(
    park: MadridPark,
    visited: Boolean,
    checkInResult: ParkCheckInResult?,
    onCheckIn: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ComposeColor.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(parkLightBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83C\uDF33", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = park.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = parkPrimary
                    )
                    if (visited || checkInResult != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(parkLightBg)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (checkInResult != null) "+${checkInResult.xpAwarded} XP" else "Visited",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = parkAccent
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = park.description,
                fontSize = 13.sp,
                color = parkMuted,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (checkInResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(parkLightBg)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "+${checkInResult.xpAwarded} XP added! Welcome to ${checkInResult.parkName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = parkAccent
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = parkAccent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Done", color = ComposeColor.White, fontWeight = FontWeight.Medium)
                }
            } else if (visited) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(parkLightBg)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Already visited",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = parkAccent
                    )
                }
            } else {
                Button(
                    onClick = onCheckIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = parkAccent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Check in here",
                        color = ComposeColor.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MarkerInfoCard(
    marker: CampusMarkerEntity,
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

