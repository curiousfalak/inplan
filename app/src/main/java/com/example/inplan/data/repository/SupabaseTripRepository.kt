package com.example.inplan.data.repository

import com.example.inplan.data.model.Trip
import com.example.inplan.domain.repository.TripMemberRepository
import com.example.inplan.domain.repository.TripRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseTripRepository @Inject constructor(
    private val client: SupabaseClient,
    private val tripMemberRepository: TripMemberRepository
) : TripRepository {

    override val currentUserIdOrNull: String?
        get() = client.auth.currentUserOrNull()?.id

    private val userId: String
        get() = currentUserIdOrNull ?: error("Not signed in")

    override suspend fun createTrip(name: String): Trip {
        val trip = client.postgrest.from("trips")
            .insert(Trip(name = name, createdBy = userId, inviteCode = generateInviteCode())) { select() }
            .decodeSingle<Trip>()
        tripMemberRepository.joinTrip(trip.id)
        return trip
    }

    override suspend fun joinTripByCode(code: String): Trip {
        val trip = client.postgrest.from("trips")
            .select { filter { eq("invite_code", code.trim().uppercase()) } }
            .decodeSingleOrNull<Trip>()
            ?: error("No trip found with that code")
        tripMemberRepository.joinTrip(trip.id)
        return trip
    }

    override suspend fun myTrips(): List<Trip> =
        client.postgrest.from("trips").select().decodeList()

    override suspend fun getTrip(tripId: String): Trip {
        TODO("Not yet implemented")
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    override suspend fun updateTripStatus(tripId: String, status: String) {
        client.postgrest.from("trips")
            .update({ set("status", status) }) { filter { eq("id", tripId) } }
    }
}