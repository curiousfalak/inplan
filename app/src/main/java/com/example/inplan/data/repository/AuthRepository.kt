package com.example.inplan.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(private val client: SupabaseClient) {

    val currentUserId: String?
        get() = client.auth.currentUserOrNull()?.id

    suspend fun signUp(email: String, password: String, fullName: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = kotlinx.serialization.json.buildJsonObject {
                put("full_name", kotlinx.serialization.json.JsonPrimitive(fullName))
            }
        }
    }

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    suspend fun updateUpiId(upiId: String) {
        val uid = currentUserId ?: error("Not signed in")
        client.postgrest.from("profiles")
            .update({ set("upi_id", upiId) }) { filter { eq("id", uid) } }
    }

    // Returns the signed-in user's actual saved UPI ID (or null if none is
    // set / blank). Used by ProfileScreen to show "already connected" status
    // instead of always presenting a blank field regardless of what's saved.
    suspend fun getMyUpiId(): String? {
        val uid = currentUserId ?: return null

        @kotlinx.serialization.Serializable
        data class UpiIdOnly(
            @kotlinx.serialization.SerialName("upi_id") val upiId: String? = null
        )

        val row = client.postgrest.from("profiles")
            .select(columns = Columns.list("upi_id")) { filter { eq("id", uid) } }
            .decodeSingleOrNull<UpiIdOnly>()

        return row?.upiId?.takeIf { it.isNotBlank() }
    }

    suspend fun hasUpiId(): Boolean = getMyUpiId() != null
}