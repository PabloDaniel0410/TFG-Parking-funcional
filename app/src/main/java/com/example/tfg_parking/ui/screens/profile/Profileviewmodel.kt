package com.example.tfg_parking.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_parking.data.model.UserProfile
import com.example.tfg_parking.data.remote.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val uid = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val result = Supabase.client
                    .postgrest["user_profiles"]
                    .select { filter { eq("id", uid) } }
                    .decodeSingleOrNull<UserProfile>()
                _profile.value = result
            } catch (_: Exception) {}
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val uid   = Supabase.client.auth.currentUserOrNull()?.id ?: return@launch
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                val path  = "$uid/avatar.jpg"
                val bucket = Supabase.client.storage["avatars"]
                bucket.upload(path, bytes) { upsert = true }

                val url = bucket.publicUrl(path)
                Supabase.client.postgrest["user_profiles"]
                    .upsert(buildJsonObject {
                        put("id",         uid)
                        put("avatar_url", url)
                    })
                _profile.value = _profile.value?.copy(avatarUrl = url)
                    ?: UserProfile(id = uid, avatarUrl = url)
            } catch (_: Exception) {}
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try { Supabase.client.auth.signOut() } catch (_: Exception) {}
        }
    }
}