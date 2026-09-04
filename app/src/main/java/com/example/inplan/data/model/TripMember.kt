package com.example.inplan.data.model



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// No standalone "id" column — trip_members is keyed on (trip_id, user_id).
@Serializable
data class TripMember(
    @SerialName("trip_id") val tripId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("joined_at") val joinedAt: String = ""
)