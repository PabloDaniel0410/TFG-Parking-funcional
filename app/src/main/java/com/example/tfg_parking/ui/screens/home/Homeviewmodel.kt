package com.example.tfg_parking.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.model.ParkingSpot
import com.example.tfg_parking.data.model.Vehicle
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class UserBalanceRow(
    val balance: Double = 0.0
)

data class HomeUiState(
    val spots: List<ParkingSpot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val favouriteSpotIds: Set<Int> = emptySet(),
    val userVehicles: List<Vehicle> = emptyList(),
    val availableCount: Int = 0,
    // Saldo del usuario para bloquear reservas sin fondos
    val userBalance: Double = 0.0
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private val pageSize = 100
    private var cameraJob: Job? = null

    init {
        fetchSpotsNear(lat = 39.4699, lng = -0.3763, radiusKm = 2.0)
        fetchUserVehicles()
        fetchFavouriteIds()
        fetchAvailableCount()
        fetchUserBalance()
    }

    // Carga plazas dentro de un bounding box centrado en lat/lng
    fun fetchSpotsNear(lat: Double, lng: Double, radiusKm: Double = 1.5) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val deltaLat = radiusKm / 111.0
                val deltaLng = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)))

                val spots = Supabase.client
                    .postgrest["parking_spots"]
                    .select {
                        filter {
                            gte("lat", lat - deltaLat)
                            lte("lat", lat + deltaLat)
                            gte("lng", lng - deltaLng)
                            lte("lng", lng + deltaLng)
                        }
                        range(0L, (pageSize - 1).toLong())
                    }
                    .decodeList<ParkingSpot>()

                _uiState.value = _uiState.value.copy(spots = spots, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando plazas"
                )
            }
        }
    }

    // Debounce de 400ms al mover el mapa para no saturar Supabase
    fun onCameraMoved(lat: Double, lng: Double) {
        cameraJob?.cancel()
        cameraJob = viewModelScope.launch {
            delay(400)
            fetchSpotsNear(lat, lng)
        }
    }

    // Cuenta real de plazas disponibles en toda la base de datos via RPC
    fun fetchAvailableCount() {
        viewModelScope.launch {
            try {
                // Llamamos a la función SQL count_available_spots() definida en Supabase
                val result = Supabase.client.postgrest
                    .rpc("count_available_spots", kotlinx.serialization.json.buildJsonObject {})
                val count = result.data.trim().trimStart('[').trimEnd(']').toIntOrNull() ?: 0
                _uiState.value = _uiState.value.copy(availableCount = count)
            } catch (_: Exception) {
                // Fallback: contar desde los spots cargados localmente
                val local = _uiState.value.spots.count { it.status == "available" }
                _uiState.value = _uiState.value.copy(availableCount = local)
            }
        }
    }

    // Carga el saldo del usuario desde user_balance
    fun fetchUserBalance() {
        viewModelScope.launch {
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val rows = Supabase.client
                    .postgrest["user_balance"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<UserBalanceRow>()
                val balance = rows.firstOrNull()?.balance ?: 0.0
                _uiState.value = _uiState.value.copy(userBalance = balance)
            } catch (_: Exception) {}
        }
    }

    fun fetchUserVehicles() {
        viewModelScope.launch {
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val vehicles = Supabase.client
                    .postgrest["vehicles"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<Vehicle>()
                _uiState.value = _uiState.value.copy(userVehicles = vehicles)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error vehículos: ${e.message}")
            }
        }
    }

    fun fetchFavouriteIds() {
        viewModelScope.launch {
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val favs = Supabase.client
                    .postgrest["favourites"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<com.example.tfg_parking.data.model.Favourite>()
                _uiState.value = _uiState.value.copy(
                    favouriteSpotIds = favs.map { it.spotId }.toSet()
                )
            } catch (_: Exception) {}
        }
    }

    fun toggleFavourite(spot: ParkingSpot) {
        viewModelScope.launch {
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val current = _uiState.value.favouriteSpotIds
                if (spot.id in current) {
                    Supabase.client.postgrest["favourites"]
                        .delete { filter { eq("spot_id", spot.id); eq("user_id", userId) } }
                    _uiState.value = _uiState.value.copy(favouriteSpotIds = current - spot.id)
                } else {
                    Supabase.client.postgrest["favourites"]
                        .insert(buildJsonObject {
                            put("user_id",   userId)
                            put("spot_id",   spot.id)
                            put("spot_name", spot.name)
                            put("address",   spot.address)
                        })
                    _uiState.value = _uiState.value.copy(favouriteSpotIds = current + spot.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun reserveSpot(spot: ParkingSpot, vehiclePlate: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }

                // Verificación de saldo antes de llamar a la RPC
                val balance = _uiState.value.userBalance
                if (balance < spot.pricePerHour) {
                    onResult(false, "Saldo insuficiente (%.2f € disponibles, se necesitan %.2f €)".format(balance, spot.pricePerHour))
                    return@launch
                }

                Supabase.client.postgrest.rpc(
                    "reserve_spot",
                    buildJsonObject {
                        put("p_spot_id",       spot.id)
                        put("p_user_id",       userId)
                        put("p_vehicle_plate", vehiclePlate)
                    }
                )

                // Refresca saldo, spots y contador tras reserva exitosa
                // Pequeña pausa para que Supabase procese el trigger antes de leer
                kotlinx.coroutines.delay(500)
                fetchUserBalance()
                val lastSpot = _uiState.value.spots.firstOrNull()
                if (lastSpot != null) fetchSpotsNear(lastSpot.lat, lastSpot.lng)
                fetchAvailableCount()
                onResult(true, "Plaza reservada correctamente")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error al reservar")
            }
        }
    }

    // Refresca todo (llamar al volver a la pantalla o al pulsar el FAB)
    fun refreshAll() {
        fetchUserBalance()
        fetchAvailableCount()
        val spots = _uiState.value.spots
        if (spots.isNotEmpty()) {
            val center = spots.first()
            fetchSpotsNear(center.lat, center.lng)
        }
    }

    fun refreshSpot(spotId: Int) {
        viewModelScope.launch {
            try {
                val updatedList = _uiState.value.spots.toMutableList()
                val idx = updatedList.indexOfFirst { it.id == spotId }
                if (idx >= 0) {
                    val refreshed = Supabase.client
                        .postgrest["parking_spots"]
                        .select { filter { eq("id", spotId) } }
                        .decodeList<ParkingSpot>()
                    if (refreshed.isNotEmpty()) {
                        updatedList[idx] = refreshed.first()
                        _uiState.value = _uiState.value.copy(spots = updatedList)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}