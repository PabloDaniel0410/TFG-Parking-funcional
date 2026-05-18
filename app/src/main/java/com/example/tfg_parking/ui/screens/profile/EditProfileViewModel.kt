package com.example.tfg_parking.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class EditProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    fun loadProfile(onLoaded: (displayName: String, phone: String) -> Unit) {
        viewModelScope.launch {
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val row = Supabase.client.postgrest["user_profiles"]
                    .select { filter { eq("id", uid) } }
                    .decodeSingleOrNull<Map<String, String>>()
                onLoaded(row?.get("display_name") ?: "", row?.get("phone") ?: "")
            } catch (_: Exception) {}
        }
    }

    fun saveAll(displayName: String, phone: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState(isLoading = true)
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: run {
                    _uiState.value = EditProfileUiState(error = "Sin sesión")
                    return@launch
                }
                // Actualizar perfil
                Supabase.client.postgrest["user_profiles"]
                    .upsert(buildJsonObject {
                        put("id",           uid)
                        put("display_name", displayName)
                        put("phone",        phone)
                    })
                // Contraseña sólo si se rellenó
                if (newPassword.isNotBlank()) {
                    if (newPassword != confirmPassword) {
                        _uiState.value = EditProfileUiState(error = "Las contraseñas no coinciden")
                        return@launch
                    }
                    if (newPassword.length < 6) {
                        _uiState.value = EditProfileUiState(error = "Mínimo 6 caracteres")
                        return@launch
                    }
                    Supabase.client.auth.updateUser { password = newPassword }
                }
                _uiState.value = EditProfileUiState(isSuccess = true)
            } catch (e: Exception) {
                _uiState.value = EditProfileUiState(error = e.message ?: "Error al guardar")
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}