package com.example.tfg_parking.ui.screens.secondary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.model.Reservation
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    init { fetchHistory() }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState(isLoading = true)
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = HistoryUiState(error = "No hay sesión activa")
                    return@launch
                }
                val reservations = Supabase.client
                    .postgrest["reservations"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<Reservation>()
                _uiState.value = HistoryUiState(reservations = reservations)
            } catch (e: Exception) {
                _uiState.value = HistoryUiState(error = e.message ?: "Error cargando historial")
            }
        }
    }
}