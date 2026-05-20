package com.example.tfg_parking.ui.screens.booking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_parking.data.model.ParkingSpot
import com.example.tfg_parking.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    spot: ParkingSpot,
    navController: NavController,
    vm: BookingViewModel = viewModel()
) {
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.navigate(Screen.History.route) {
                popUpTo(Screen.Home.route)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de plaza") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info de la plaza
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(spot.name, style = MaterialTheme.typography.titleMedium)
                    if (spot.address.isNotBlank())
                        Text(spot.address, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${spot.pricePerHour} €/hora",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val (statusLabel, statusColor) = when (spot.status) {
                        "reserved" -> "Reservada" to MaterialTheme.colorScheme.tertiary
                        "occupied" -> "Ocupada"   to MaterialTheme.colorScheme.error
                        else       -> "Libre"     to MaterialTheme.colorScheme.primary
                    }
                    Text(statusLabel, color = statusColor, style = MaterialTheme.typography.labelMedium)
                }
            }

            // Nota informativa: la reserva se gestiona desde el mapa
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info, null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        "Las reservas se realizan directamente desde el mapa pulsando sobre una plaza libre.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Si la plaza está reservada por este usuario, mostrar botón de confirmar llegada
            if (spot.status == "reserved" && spot.assignedVehicle != null) {
                Spacer(Modifier.weight(1f))

                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick  = { vm.confirmArrival(spot, spot.assignedVehicle) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled  = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(8.dp))
                        Text("He llegado a la plaza")
                    }
                }
            }
        }
    }
}