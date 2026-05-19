package com.example.tfg_parking.ui.screens.home

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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_parking.data.model.ParkingSpot
import com.example.tfg_parking.navigation.Screen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onReserveSpot: (ParkingSpot) -> Unit = {},
    vm: HomeViewModel = viewModel()
) {
    val state        by vm.uiState.collectAsState()
    var selectedSpot by remember { mutableStateOf<ParkingSpot?>(null) }

    // ✅ Retrasamos la composición del mapa 1 frame para no bloquear el primer render
    var showMap by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)   // deja que la UI inicial se pinte antes de inicializar el mapa
        showMap = true
    }

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
            if (showMap) {
                MapView(
                    spots       = state.spots,
                    onSpotClick = { selectedSpot = it },
                    modifier    = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder mientras Maps inicializa — evita el jank en el primer frame
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            state.error?.let { err ->
                Card(Modifier.align(Alignment.TopCenter).padding(16.dp)) {
                    Text(err, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
                }
            }

            selectedSpot?.let { spot ->
                SpotBottomCard(
                    spot      = spot,
                    onDismiss = { selectedSpot = null },
                    onReserve = { onReserveSpot(spot) },
                    modifier  = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (selectedSpot == null && showMap) {
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

@Composable
fun MapView(
    spots: List<ParkingSpot>,
    onSpotClick: (ParkingSpot) -> Unit,
    modifier: Modifier = Modifier
) {
    val context      = LocalContext.current
    val googleMap    = remember { mutableStateOf<GoogleMap?>(null) }
    val currentSpots = rememberUpdatedState(spots)
    val currentClick = rememberUpdatedState(onSpotClick)

    val mapView = remember { com.google.android.gms.maps.MapView(context) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        mapView.onCreate(null)
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner)   = mapView.onStart()
            override fun onResume(owner: LifecycleOwner)  = mapView.onResume()
            override fun onPause(owner: LifecycleOwner)   = mapView.onPause()
            override fun onStop(owner: LifecycleOwner)    = mapView.onStop()
            override fun onDestroy(owner: LifecycleOwner) = mapView.onDestroy()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    AndroidView(factory = { mapView }, modifier = modifier)

    LaunchedEffect(Unit) {
        mapView.getMapAsync { map ->
            googleMap.value = map
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(39.4699, -0.3763), 13f))
            updateMarkers(map, currentSpots.value, currentClick.value)
        }
    }

    LaunchedEffect(spots) {
        googleMap.value?.let { map ->
            updateMarkers(map, currentSpots.value, currentClick.value)
        }
    }
}

private fun updateMarkers(
    map: GoogleMap,
    spots: List<ParkingSpot>,
    onSpotClick: (ParkingSpot) -> Unit
) {
    map.clear()
    spots.forEach { spot ->
        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(spot.lat, spot.lng))
                .title(spot.name)
                .snippet(if (spot.isAvailable) "Libre · ${spot.pricePerHour} €/h" else "Ocupada")
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        if (spot.isAvailable) BitmapDescriptorFactory.HUE_GREEN
                        else BitmapDescriptorFactory.HUE_RED
                    )
                )
        )
        marker?.tag = spot
    }
    map.setOnMarkerClickListener { marker ->
        (marker.tag as? ParkingSpot)?.let { onSpotClick(it) }
        true
    }
}

@Composable
private fun SpotBottomCard(
    spot: ParkingSpot,
    onDismiss: () -> Unit,
    onReserve: () -> Unit,
    modifier: Modifier = Modifier
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
                    onClick     = {},
                    label       = { Text(if (spot.isAvailable) "Libre" else "Ocupada") },
                    leadingIcon = {
                        Icon(
                            imageVector        = if (spot.isAvailable) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint               = if (spot.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
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
                    onClick  = onReserve,
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