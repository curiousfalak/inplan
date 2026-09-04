package com.example.inplan.data.model



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    val id: String = "",
    val name: String,
    @SerialName("created_by") val createdBy: String,
    val status: String = "active",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("invite_code") val inviteCode: String,
    @SerialName("upi_id") val upiId: String? = null
)

