package com.example.tfg_parking.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.model.ParkingSpot
import com.example.tfg_parking.data.model.Vehicle
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class HomeUiState(
    val spots: List<ParkingSpot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val favouriteSpotIds: Set<Int> = emptySet(),
    val userVehicles: List<Vehicle> = emptyList(),
    val availableCount: Int = 0
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var currentPage = 0
    private val pageSize = 100
    private val allSpots = mutableListOf<ParkingSpot>()

    init {
        fetchSpots()
        fetchUserVehicles()
        fetchFavouriteIds()
    }

    fun fetchSpots(page: Int = 0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val from = page * pageSize
                val to = from + pageSize - 1
                val spots = Supabase.client
                    .postgrest["parking_spots"]
                    .select { range(from.toLong(), to.toLong()) }
                    .decodeList<ParkingSpot>()

                if (page == 0) allSpots.clear()
                allSpots.addAll(spots)
                currentPage = page

                val available = allSpots.count { it.status == "available" || it.isAvailable }
                _uiState.value = _uiState.value.copy(
                    spots = allSpots.toList(),
                    isLoading = false,
                    availableCount = available
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error cargando plazas"
                )
            }
        }
    }

    fun fetchNextPage() {
        fetchSpots(currentPage + 1)
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
            } catch (_: Exception) {}
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
                    _uiState.value = _uiState.value.copy(
                        favouriteSpotIds = current - spot.id
                    )
                } else {
                    Supabase.client.postgrest["favourites"]
                        .insert(buildJsonObject {
                            put("user_id",   userId)
                            put("spot_id",   spot.id)
                            put("spot_name", spot.name)
                            put("address",   spot.address)
                        })
                    _uiState.value = _uiState.value.copy(
                        favouriteSpotIds = current + spot.id
                    )
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

                val result = Supabase.client.postgrest.rpc(
                    "reserve_spot",
                    buildJsonObject {
                        put("p_spot_id",        spot.id)
                        put("p_user_id",        userId)
                        put("p_vehicle_plate",  vehiclePlate)
                    }
                )

                fetchSpots(currentPage)
                onResult(true, "Plaza reservada correctamente")
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error al reservar")
            }
        }
    }
    fun refreshSpot(spotId: Int) {
        viewModelScope.launch {
            try {
                val updatedList = allSpots.toMutableList()
                val idx = updatedList.indexOfFirst { it.id == spotId }
                if (idx >= 0) {
                    val refreshed = Supabase.client
                        .postgrest["parking_spots"]
                        .select { filter { eq("id", spotId) } }
                        .decodeList<ParkingSpot>()
                    if (refreshed.isNotEmpty()) {
                        updatedList[idx] = refreshed.first()
                        allSpots.clear()
                        allSpots.addAll(updatedList)
                        val available = allSpots.count { it.status == "available" || it.isAvailable }
                        _uiState.value = _uiState.value.copy(
                            spots = allSpots.toList(),
                            availableCount = available
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }
}