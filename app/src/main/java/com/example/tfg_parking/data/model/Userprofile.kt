package com.example.tfg_parking.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    @SerialName("display_name") val displayName: String  = "",
    @SerialName("avatar_url")   val avatarUrl:   String  = "",
    val phone:                                   String  = "",
    @SerialName("dark_mode")    val darkMode:    Boolean = false,
    @SerialName("accent_color") val accentColor: String  = "blue",
    @SerialName("updated_at")   val updatedAt:   String  = "",
    // Saldo del usuario en la app
    val balance: Double = 0.0
)