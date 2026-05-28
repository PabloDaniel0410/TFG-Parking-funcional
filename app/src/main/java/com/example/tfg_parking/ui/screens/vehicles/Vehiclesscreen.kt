package com.example.tfg_parking.ui.screens.vehicles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tfg_parking.data.model.Vehicle
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// ── ViewModel ──────────────────────────────────────────────────────────────
data class VehiclesUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class VehiclesViewModel : ViewModel() {
    private val _state = MutableStateFlow(VehiclesUiState())
    val state: StateFlow<VehiclesUiState> = _state

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = VehiclesUiState(isLoading = true)
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    _state.value = VehiclesUiState(error = "Sin sesión"); return@launch
                }
                val list = Supabase.client.postgrest["vehicles"]
                    .select { filter { eq("user_id", uid) } }
                    .decodeList<Vehicle>()
                _state.value = VehiclesUiState(vehicles = list)
            } catch (e: Exception) {
                _state.value = VehiclesUiState(error = e.message)
            }
        }
    }

    fun add(plate: String, type: String, eco: String) {
        viewModelScope.launch {
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch

                Supabase.client.postgrest["vehicles"].insert(buildJsonObject {
                    put("user_id",      uid)
                    put("plate",        plate.uppercase())
                    put("vehicle_type", type)
                    put("eco_label",    eco)
                })
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["vehicles"].delete { filter { eq("id", id) } }
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}

// ── Pantalla ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(navController: NavController, vm: VehiclesViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis vehículos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, "Añadir vehículo")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(
                    state.error!!, Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.error
                )
                state.vehicles.isEmpty() -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DirectionsCar, null, Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline)
                    Text("No tienes vehículos añadidos", color = MaterialTheme.colorScheme.outline)
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.vehicles, key = { it.id }) { v ->
                        VehicleCard(v, onDelete = { vm.delete(v.id) })
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddVehicleDialog(
            onDismiss = { showDialog = false },
            onAdd     = { plate, type, eco ->
                vm.add(plate, type, eco)
                showDialog = false
            }
        )
    }
}

@Composable
private fun VehicleCard(vehicle: Vehicle, onDelete: () -> Unit) {
    val typeIcon = when (vehicle.vehicleType) {
        "motorcycle" -> Icons.Default.TwoWheeler
        "van"        -> Icons.Default.LocalShipping
        "electric"   -> Icons.Default.ElectricCar
        else         -> Icons.Default.DirectionsCar
    }
    val ecoColor = when (vehicle.ecoLabel) {
        "ZERO" -> MaterialTheme.colorScheme.primary
        "ECO"  -> MaterialTheme.colorScheme.tertiary
        "C"    -> MaterialTheme.colorScheme.secondary
        else   -> MaterialTheme.colorScheme.outline
    }

    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(typeIcon, null, Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(vehicle.plate, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (vehicle.vehicleType) {
                            "motorcycle" -> "Moto"
                            "van"        -> "Furgoneta"
                            "electric"   -> "Eléctrico"
                            else         -> "Coche"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (vehicle.ecoLabel != "none") {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(vehicle.ecoLabel, style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = ecoColor
                            )
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddVehicleDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var plate by remember { mutableStateOf("") }
    var type  by remember { mutableStateOf("car") }
    var eco   by remember { mutableStateOf("none") }

    val vehicleTypes = listOf("car" to "Coche", "motorcycle" to "Moto",
        "van" to "Furgoneta", "electric" to "Eléctrico")
    val ecoLabels    = listOf("none" to "Sin etiqueta", "B" to "B",
        "C" to "C", "ECO" to "ECO", "ZERO" to "ZERO")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir vehículo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it.uppercase() },
                    label = { Text("Matrícula") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, null) }
                )

                Text("Tipo de vehículo", style = MaterialTheme.typography.labelMedium)
                vehicleTypes.forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = type == value, onClick = { type = value })
                        Text(label)
                    }
                }

                Text("Etiqueta medioambiental", style = MaterialTheme.typography.labelMedium)
                ecoLabels.forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = eco == value, onClick = { eco = value })
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (plate.isNotBlank()) onAdd(plate, type, eco) },
                enabled = plate.isNotBlank()
            ) { Text("Añadir") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}