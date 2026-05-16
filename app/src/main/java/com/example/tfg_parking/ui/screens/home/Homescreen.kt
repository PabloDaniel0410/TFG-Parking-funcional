package com.example.tfg_parking.ui.screens.home


import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_parking.data.model.ParkingSpot
import com.example.tfg_parking.navigation.Screen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.tfg_parking.R
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.compose.runtime.DisposableEffect

// ─── ID estable para el fragment (no usar View.generateViewId() — no es estable) ───
private const val MAP_FRAGMENT_TAG = "HOME_MAP_FRAGMENT"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: HomeViewModel = viewModel()
) {
    val state       by vm.uiState.collectAsState()
    var selectedSpot by remember { mutableStateOf<ParkingSpot?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TFG Parking") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.Person, "Perfil")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, "Ajustes")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick  = {},
                    icon     = { Icon(Icons.Default.Map, null) },
                    label    = { Text("Mapa") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick  = { navController.navigate(Screen.Favourites.route) },
                    icon     = { Icon(Icons.Default.Favorite, null) },
                    label    = { Text("Favoritos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick  = { navController.navigate(Screen.History.route) },
                    icon     = { Icon(Icons.Default.History, null) },
                    label    = { Text("Historial") }
                )
            }
        }
    ) { padding ->

        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Mapa usando SupportMapFragment dentro de AndroidView ────────
            MapView(
                spots        = state.spots,
                onSpotClick  = { selectedSpot = it },
                modifier     = Modifier.fillMaxSize()
            )

            // ── Loading ────────────────────────────────────────────────────
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            // ── Error banner ───────────────────────────────────────────────
            state.error?.let { err ->
                Card(Modifier.align(Alignment.TopCenter).padding(16.dp)) {
                    Text(err, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
                }
            }

            // ── Tarjeta de plaza seleccionada ──────────────────────────────
            selectedSpot?.let { spot ->
                SpotBottomCard(
                    spot      = spot,
                    onDismiss = { selectedSpot = null },
                    modifier  = Modifier.align(Alignment.BottomCenter)
                )
            }

            // ── FAB refresco (solo visible si no hay tarjeta abierta) ──────
            if (selectedSpot == null) {
                FloatingActionButton(
                    onClick  = { vm.fetchSpots() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, "Actualizar")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composable que aloja SupportMapFragment mediante AndroidView
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MapView(
    spots: List<ParkingSpot>,
    onSpotClick: (ParkingSpot) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapView = rememberMapViewWithLifecycle()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    LaunchedEffect(spots, googleMap) {
        googleMap?.let { map ->
            map.clear()
            spots.forEach { spot ->
                val pos = LatLng(spot.latitude, spot.longitude)
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(pos)
                        .title(spot.name)
                        .icon(
                            if (spot.available)
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            else
                                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                )
                marker?.tag = spot
            }
            map.setOnMarkerClickListener { marker ->
                (marker.tag as? ParkingSpot)?.let { onSpotClick(it) }
                true
            }
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    ) { mv ->
        mv.getMapAsync { map ->
            googleMap = map
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(39.4699, -0.3763), 13f)
            )
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(): com.google.android.gms.maps.MapView {
    val context = LocalContext.current
    val mapView = remember { com.google.android.gms.maps.MapView(context) }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        val observer = object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onCreate(owner: androidx.lifecycle.LifecycleOwner) = mapView.onCreate(null)
            override fun onStart(owner: androidx.lifecycle.LifecycleOwner) = mapView.onStart()
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) = mapView.onResume()
            override fun onPause(owner: androidx.lifecycle.LifecycleOwner) = mapView.onPause()
            override fun onStop(owner: androidx.lifecycle.LifecycleOwner) = mapView.onStop()
            override fun onDestroy(owner: androidx.lifecycle.LifecycleOwner) = mapView.onDestroy()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return mapView
}

// ─────────────────────────────────────────────────────────────────────────────
// Añade marcadores al mapa para cada plaza de parking
// ─────────────────────────────────────────────────────────────────────────────
private fun addSpotMarkers(
    gMap:        GoogleMap,
    spots:       List<ParkingSpot>,
    onSpotClick: (ParkingSpot) -> Unit
) {
    // Mapa auxiliar para recuperar el ParkingSpot al pulsar el marcador
    val markerToSpot = mutableMapOf<com.google.android.gms.maps.model.Marker, ParkingSpot>()

    spots.forEach { spot ->
        val hue    = if (spot.isAvailable) BitmapDescriptorFactory.HUE_GREEN
                     else                  BitmapDescriptorFactory.HUE_RED
        val snippet = if (spot.isAvailable) "Libre · ${spot.pricePerHour} €/h" else "Ocupada"

        val marker = gMap.addMarker(
            MarkerOptions()
                .position(LatLng(spot.lat, spot.lng))
                .title(spot.name)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(hue))
        )
        marker?.let { markerToSpot[it] = spot }
    }

    gMap.setOnMarkerClickListener { marker ->
        markerToSpot[marker]?.let { onSpotClick(it) }
        false // false → comportamiento por defecto (muestra InfoWindow)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarjeta inferior con info de la plaza seleccionada
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SpotBottomCard(
    spot:     ParkingSpot,
    onDismiss: () -> Unit,
    modifier:  Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(spot.name, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Cerrar")
                }
            }

            if (spot.address.isNotBlank()) {
                Text(spot.address, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick      = {},
                    label        = { Text(if (spot.isAvailable) "Libre" else "Ocupada") },
                    leadingIcon  = {
                        Icon(
                            imageVector = if (spot.isAvailable) Icons.Default.CheckCircle
                                          else                  Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (spot.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                )
                AssistChip(
                    onClick     = {},
                    label       = { Text("${spot.pricePerHour} €/h") },
                    leadingIcon = { Icon(Icons.Default.Euro, null) }
                )
            }

            if (spot.isAvailable) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick  = { /* TODO: implementar reserva */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BookmarkAdd, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reservar plaza")
                }
            }
        }
    }
}
