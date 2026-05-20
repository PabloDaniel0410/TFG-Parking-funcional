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
import com.example.tfg_parking.data.model.Vehicle
import com.example.tfg_parking.navigation.Screen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onReserveSpot: (ParkingSpot) -> Unit = {},
    vm: HomeViewModel = viewModel()
) {
    val state        by vm.uiState.collectAsState()
    var selectedSpot by remember { mutableStateOf<ParkingSpot?>(null) }
    var showMap      by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(100)
        showMap = true
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("TFG Parking") },
                actions = {
                    AssistChip(
                        onClick = {},
                        label   = { Text("${state.availableCount} libres", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Circle, null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(10.dp)
                            )
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    )
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
                    spots        = state.spots,
                    onSpotClick  = { selectedSpot = it },
                    onCameraIdle = {},
                    modifier     = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    spot         = spot,
                    isFavourite  = spot.id in state.favouriteSpotIds,
                    userVehicles = state.userVehicles,
                    onDismiss    = { selectedSpot = null },
                    onToggleFav  = { vm.toggleFavourite(spot) },
                    onReserve    = { vehiclePlate ->
                        vm.reserveSpot(spot, vehiclePlate) { success, msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                            if (success) selectedSpot = null
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            if (selectedSpot == null && showMap) {
                FloatingActionButton(
                    onClick  = { vm.fetchSpots() },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
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
    onCameraIdle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context      = LocalContext.current
    val googleMap    = remember { mutableStateOf<GoogleMap?>(null) }
    val currentSpots = rememberUpdatedState(spots)
    val currentClick = rememberUpdatedState(onSpotClick)
    val mapView      = remember { com.google.android.gms.maps.MapView(context) }

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

            map.setOnCameraIdleListener {
                val bounds = map.projection.visibleRegion.latLngBounds
                val visible = currentSpots.value.filter { spot ->
                    bounds.contains(LatLng(spot.lat, spot.lng))
                }.take(100)
                updateMarkers(map, visible, currentClick.value)
                onCameraIdle()
            }

            updateMarkers(map, currentSpots.value.take(100), currentClick.value)
        }
    }

    LaunchedEffect(spots) {
        googleMap.value?.let { map ->
            val bounds = map.projection.visibleRegion.latLngBounds
            val visible = currentSpots.value.filter { spot ->
                bounds.contains(LatLng(spot.lat, spot.lng))
            }.take(100)
            updateMarkers(map, visible, currentClick.value)
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
        val hue = when {
            spot.status == "reserved"                        -> BitmapDescriptorFactory.HUE_YELLOW
            spot.status == "occupied" || !spot.isAvailable  -> BitmapDescriptorFactory.HUE_RED
            else                                             -> BitmapDescriptorFactory.HUE_GREEN
        }
        val snippet = when (spot.status) {
            "reserved" -> "Reservada · ${spot.pricePerHour} €/h"
            "occupied" -> "Ocupada"
            else       -> "Libre · ${spot.pricePerHour} €/h"
        }
        val marker = map.addMarker(
            MarkerOptions()
                .position(LatLng(spot.lat, spot.lng))
                .title(spot.name)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(hue))
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
    isFavourite: Boolean,
    userVehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onToggleFav: () -> Unit,
    onReserve: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReserveDialog by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(30 * 60) }

    val isAvailable = spot.status == "available" || (spot.status != "reserved" && spot.status != "occupied" && spot.isAvailable)
    val isReserved  = spot.status == "reserved"
    val isOccupied  = spot.status == "occupied" || (!spot.isAvailable && spot.status != "reserved")

    LaunchedEffect(spot.id, isReserved) {
        if (isReserved) {
            spot.reservedAtTs?.let { ts ->
                try {
                    val reservedEpoch = java.time.Instant.parse(ts).epochSecond
                    val now = java.time.Instant.now().epochSecond
                    val elapsed = (now - reservedEpoch).toInt()
                    secondsLeft = (30 * 60 - elapsed).coerceAtLeast(0)
                } catch (_: Exception) {
                    secondsLeft = 30 * 60
                }
            } ?: run { secondsLeft = 30 * 60 }

            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
        }
    }

    if (showReserveDialog) {
        VehicleSelectDialog(
            vehicles  = userVehicles,
            spot      = spot,
            onConfirm = { plate ->
                showReserveDialog = false
                onReserve(plate)
            },
            onDismiss = { showReserveDialog = false }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(spot.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))

                // ❤️ Favorito
                IconButton(onClick = onToggleFav) {
                    Icon(
                        imageVector        = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavourite) "Quitar de favoritos" else "Añadir a favoritos",
                        tint               = if (isFavourite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Cerrar")
                }
            }

            if (spot.address.isNotBlank()) {
                Text(spot.address, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val (statusLabel, statusIcon, statusTint) = when {
                    isReserved -> Triple("Reservada", Icons.Default.Schedule,     Color(0xFFF9A825))
                    isOccupied -> Triple("Ocupada",   Icons.Default.Cancel,       Color(0xFFC62828))
                    else       -> Triple("Libre",     Icons.Default.CheckCircle,  Color(0xFF2E7D32))
                }
                AssistChip(
                    onClick     = {},
                    label       = { Text(statusLabel) },
                    leadingIcon = { Icon(statusIcon, null, tint = statusTint, modifier = Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick     = {},
                    label       = { Text("${spot.pricePerHour} €/h") },
                    leadingIcon = { Icon(Icons.Default.Euro, null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Contador regresivo (solo en plazas reservadas)
            if (isReserved) {
                Spacer(Modifier.height(8.dp))
                val min   = secondsLeft / 60
                val sec   = secondsLeft % 60
                val color = if (secondsLeft < 300) Color(0xFFC62828) else Color(0xFFF9A825)
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Timer, null, tint = color, modifier = Modifier.size(20.dp))
                    Text(
                        text  = "Tiempo para llegar: %02d:%02d".format(min, sec),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                }
                if (spot.assignedVehicle != null) {
                    Text(
                        text  = "Vehículo: ${spot.assignedVehicle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (isAvailable) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick  = { showReserveDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BookmarkAdd, null)
                    Spacer(Modifier.width(8.dp))
                    Text("¿Quiere reservar esta plaza?")
                }
            }
        }
    }
}

@Composable
private fun VehicleSelectDialog(
    vehicles: List<Vehicle>,
    spot: ParkingSpot,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPlate by remember {
        mutableStateOf(vehicles.firstOrNull { it.isDefault }?.plate ?: vehicles.firstOrNull()?.plate ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Default.DirectionsCar, null) },
        title = { Text("Selecciona un vehículo") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Plaza: ${spot.name}\nPenalización si no llegas a tiempo: ${spot.pricePerHour} €",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(4.dp))

                if (vehicles.isEmpty()) {
                    Text(
                        "No tienes vehículos registrados. Añade uno en tu perfil.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    vehicles.forEach { v ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier          = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedPlate == v.plate,
                                onClick  = { selectedPlate = v.plate }
                            )
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(v.plate, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${v.vehicleType} · ${v.ecoLabel}${if (v.isDefault) " · (por defecto)" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { if (selectedPlate.isNotBlank()) onConfirm(selectedPlate) },
                enabled  = selectedPlate.isNotBlank()
            ) { Text("Confirmar reserva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}