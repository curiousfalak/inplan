package com.example.inplan.data.repository



import com.example.inplan.domain.repository.TripMemberRepository
import com.example.inplan.data.model.TripMember

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseTripMemberRepository @Inject constructor(
    private val client: SupabaseClient
) : TripMemberRepository {

    private val userId: String
        get() = client.auth.currentUserOrNull()?.id ?: error("Not signed in")

    override suspend fun joinTrip(tripId: String) {
        try {
            client.postgrest.from("trip_members").insert(TripMember(tripId = tripId, userId = userId))
        } catch (e: Exception) {
            // Only swallow if we're already a member (e.g. a unique-constraint
            // conflict on rejoin). Any other failure — RLS denial, network,
            // a bad trip id — should still surface to the caller.
            val alreadyMember = getTripMembers(tripId).contains(userId)
            if (!alreadyMember) throw e
        }
    }

    override suspend fun getTripMembers(tripId: String): List<String> =
        client.postgrest.from("trip_members")
            .select { filter { eq("trip_id", tripId) } }
            .decodeList<TripMember>()
            .map { it.userId }

    override fun observeTripMembers(tripId: String): Flow<List<String>> = flow {
        emit(getTripMembers(tripId))
        val channel = client.channel("trip-members-$tripId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "trip_members"
        }
        channel.subscribe()
        changes.collect { emit(getTripMembers(tripId)) }
    }
}