package com.example.tfg_parking.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParkingSpot(
    val id: Int = 0,
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    @SerialName("is_available")   val isAvailable:  Boolean = true,
    @SerialName("price_per_hour") val pricePerHour: Double  = 0.0,
    val address: String = "",
    @SerialName("spot_type")      val spotType:     String  = "normal",
    // 'normal' | 'disabled' | 'electric'
    val zone: String = "centro"
    // 'centro' | 'medio' | 'periferia'
)