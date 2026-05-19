package com.example.tfg_parking.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.model.ParkingSpot
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val spots: List<ParkingSpot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { fetchSpots() }

    fun fetchSpots() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val spots = Supabase.client
                    .postgrest["parking_spots"]
                    .select()
                    .decodeList<ParkingSpot>()
                    .take(100)   // Limita a 100 spots para evitar ANR en el hilo principal
                _uiState.value = HomeUiState(spots = spots)
            } catch (e: Exception) {
                _uiState.value = HomeUiState(error = e.message ?: "Error cargando plazas")
            }
        }
    }
}