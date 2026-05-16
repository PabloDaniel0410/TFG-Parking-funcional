package com.example.tfg_parking.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class EditProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    fun updatePassword(newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _uiState.value = EditProfileUiState(error = "Las contraseñas no coinciden")
            return
        }
        if (newPassword.length < 6) {
            _uiState.value = EditProfileUiState(error = "Mínimo 6 caracteres")
            return
        }
        viewModelScope.launch {
            _uiState.value = EditProfileUiState(isLoading = true)
            try {
                Supabase.client.auth.updateUser { password = newPassword }
                _uiState.value = EditProfileUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = EditProfileUiState(error = e.message ?: "Error al actualizar")
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}