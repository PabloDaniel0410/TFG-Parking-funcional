package com.example.tfg_parking.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethod(
    val id: Int = 0,
    @SerialName("user_id")      val userId:     String  = "",
    @SerialName("method_type")  val methodType: String  = "card",
    val label:                                  String  = "",
    val balance:                                Double  = 0.0,
    @SerialName("is_default")   val isDefault:  Boolean = false,
    @SerialName("created_at")   val createdAt:  String  = ""
)