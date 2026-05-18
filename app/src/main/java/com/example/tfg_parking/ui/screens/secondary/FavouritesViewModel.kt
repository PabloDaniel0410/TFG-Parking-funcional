package com.example.tfg_parking.ui.screens.secondary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.model.Favourite
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FavouritesUiState(
    val favourites: List<Favourite> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FavouritesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FavouritesUiState())
    val uiState: StateFlow<FavouritesUiState> = _uiState

    init {
        fetchFavourites()
    }

    fun fetchFavourites() {
        viewModelScope.launch {
            _uiState.value = FavouritesUiState(isLoading = true)
            try {
                val userId = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = FavouritesUiState(error = "No hay sesión activa")
                    return@launch
                }
                val favs = Supabase.client
                    .postgrest["favourites"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<Favourite>()
                _uiState.value = FavouritesUiState(favourites = favs)
            } catch (e: Exception) {
                _uiState.value = FavouritesUiState(error = e.message ?: "Error cargando favoritos")
            }
        }
    }

    fun removeFavourite(id: Int) {
        viewModelScope.launch {
            try {
                Supabase.client.postgrest["favourites"].delete { filter { eq("id", id) } }
                // Actualiza el estado local directamente
                _uiState.value = _uiState.value.copy(
                    favourites = _uiState.value.favourites.filter { it.id != id }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Error al eliminar")
            }
        }
    }
}