package com.example.inplan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = "",
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("upi_id") val upiId: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)