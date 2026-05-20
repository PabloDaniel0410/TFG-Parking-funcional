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
    val error: String? = null,
    val showClearConfirm: Boolean = false
)

class HistoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    init { fetchHistory() }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "No hay sesión activa")
                    return@launch
                }
                val reservations = Supabase.client
                    .postgrest["reservations"]
                    .select {
                        filter { eq("user_id", userId) }
                        order("reserved_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    }
                    .decodeList<Reservation>()
                _uiState.value = _uiState.value.copy(isLoading = false, reservations = reservations)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error cargando historial")
            }
        }
    }

    fun requestClearHistory() {
        _uiState.value = _uiState.value.copy(showClearConfirm = true)
    }

    fun dismissClearConfirm() {
        _uiState.value = _uiState.value.copy(showClearConfirm = false)
    }

    fun clearHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showClearConfirm = false)
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                Supabase.client
                    .postgrest["reservations"]
                    .delete { filter { eq("user_id", userId) } }
                _uiState.value = _uiState.value.copy(isLoading = false, reservations = emptyList())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Error al borrar historial")
            }
        }
    }
}