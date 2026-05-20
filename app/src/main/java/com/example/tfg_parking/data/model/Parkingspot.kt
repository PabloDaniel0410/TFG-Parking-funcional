package com.example.tfg_parking.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParkingSpot(
    val id: Int = 0,
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    @SerialName("is_available")   val isAvailable:   Boolean = true,
    @SerialName("price_per_hour") val pricePerHour:  Double  = 0.0,
    val address: String = "",
    @SerialName("spot_type")      val spotType:      String  = "normal",
    val zone: String = "centro",
    // Estado: "available" | "occupied" | "reserved"
    val status: String = "available",
    // Vehículo asignado a la plaza (matricula o null)
    @SerialName("assigned_vehicle") val assignedVehicle: String? = null,
    // Usuario que ha reservado
    @SerialName("reserved_by")    val reservedBy:    String? = null,
    // Momento de reserva (ISO string)
    @SerialName("reserved_at")    val reservedAtTs:  String? = null
)