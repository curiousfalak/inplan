package com.example.inplan.data.repository

import com.example.inplan.domain.repository.ProfileRepository
import com.example.inplan.data.model.Profile

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseProfileRepository @Inject constructor(
    private val client: SupabaseClient
) : ProfileRepository {

    override suspend fun getProfiles(userIds: List<String>): List<Profile> =
        client.postgrest.from("profiles")
            .select { filter { isIn("id", userIds) } }
            .decodeList()

    override suspend fun updateUpiId(userId: String, upiId: String): Result<Unit> = runCatching {
        client.postgrest.from("profiles")
            .update(mapOf("upi_id" to upiId)) {
                filter { eq("id", userId) }
            }
        Unit
    }
}