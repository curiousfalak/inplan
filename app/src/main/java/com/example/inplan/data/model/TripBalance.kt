package com.example.inplan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Matches RETURNS TABLE(user_id uuid, full_name text, net_balance numeric)
// from get_trip_balances(_trip_id). full_name comes pre-joined from profiles,
// so no separate lookup is needed to render a balances list.
@Serializable
data class TripBalance(
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("net_balance") val netBalance: Double
)

// The RPC parameter is named "_trip_id" (leading underscore), not "trip_id" —
// Supabase's .rpc() matches by this exact name, so it must match the SQL
// function signature precisely or the call fails at runtime.
@Serializable
data class TripIdParam(
    @SerialName("_trip_id") val tripId: String
)