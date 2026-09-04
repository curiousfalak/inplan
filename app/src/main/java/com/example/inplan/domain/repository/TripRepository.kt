package com.example.inplan.domain.repository

import com.example.inplan.data.model.Trip

interface TripRepository {
    // Added: SupabaseTripRepository overrides this to expose the signed-in
    // user's id, computed live from the Supabase auth client. It needs to
    // live on the interface for that override to be valid.
    val currentUserIdOrNull: String?

    suspend fun createTrip(name: String): Trip
    suspend fun joinTripByCode(code: String): Trip
    suspend fun myTrips(): List<Trip>
    suspend fun getTrip(tripId: String): Trip

    suspend fun updateTripStatus(tripId: String, status: String)
}