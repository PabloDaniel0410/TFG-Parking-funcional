package com.example.tfg_parking.ui.screens.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.model.ParkingSpot
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class BookingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class BookingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState

    /**
     * Confirma la llegada del usuario a la plaza reservada.
     * Si llega antes de 30 min → Supabase devuelve la penalización al balance.
     * NO se cobra nada aquí; el cobro fue en la reserva (HomeViewModel).
     */
    fun confirmArrival(spot: ParkingSpot, vehiclePlate: String) {
        viewModelScope.launch {
            _uiState.value = BookingUiState(isLoading = true)
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = BookingUiState(error = "No hay sesión activa")
                    return@launch
                }

                Supabase.client.postgrest.rpc(
                    "confirm_arrival",
                    buildJsonObject {
                        put("p_spot_id",       spot.id)
                        put("p_user_id",       userId)
                        put("p_vehicle_plate", vehiclePlate)
                    }
                )

                _uiState.value = BookingUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = BookingUiState(error = e.message ?: "Error al confirmar llegada")
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}