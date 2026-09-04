package com.example.inplan.domain.repository

import kotlinx.coroutines.flow.Flow

interface TripMemberRepository {
    suspend fun joinTrip(tripId: String)
    suspend fun getTripMembers(tripId: String): List<String>
    fun observeTripMembers(tripId: String): Flow<List<String>>
}