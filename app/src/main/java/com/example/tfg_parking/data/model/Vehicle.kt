package com.example.tfg_parking.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vehicle(
    val id: Int = 0,
    @SerialName("user_id")      val userId:      String  = "",
    val plate:                                   String  = "",
    @SerialName("vehicle_type") val vehicleType: String  = "car",
    @SerialName("eco_label")    val ecoLabel:    String  = "none",
    @SerialName("is_default")   val isDefault:   Boolean = false,
    @SerialName("created_at")   val createdAt:   String  = ""
)