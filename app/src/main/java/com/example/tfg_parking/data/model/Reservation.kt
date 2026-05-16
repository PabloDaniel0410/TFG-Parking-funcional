package com.example.tfg_parking.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Reservation(
    val id: Int = 0,
    @SerialName("user_id")        val userId:        String = "",
    @SerialName("spot_id")        val spotId:        Int    = 0,
    @SerialName("spot_name")      val spotName:      String = "",
    val address:                  String = "",
    @SerialName("reserved_at")    val reservedAt:    String = "",
    @SerialName("duration_hours") val durationHours: Int    = 1,
    @SerialName("total_price")    val totalPrice:    Double = 0.0
)