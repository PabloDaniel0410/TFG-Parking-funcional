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

    fun reserve(spot: ParkingSpot, durationHours: Int) {
        viewModelScope.launch {
            _uiState.value = BookingUiState(isLoading = true)
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = BookingUiState(error = "No hay sesión activa")
                    return@launch
                }
                val totalPrice = spot.pricePerHour * durationHours
                Supabase.client
                    .postgrest["reservations"]
                    .insert(buildJsonObject {
                        put("user_id",        userId)
                        put("spot_id",        spot.id)
                        put("spot_name",      spot.name)
                        put("address",        spot.address)
                        put("duration_hours", durationHours)
                        put("total_price",    totalPrice)
                    })
                _uiState.value = BookingUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = BookingUiState(error = e.message ?: "Error al reservar")
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}