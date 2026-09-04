package com.example.inplan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Poll(
    val id: String = "",
    @SerialName("trip_id") val tripId: String,
    val title: String,
    // Nullable — a poll can be a pure coordination "Plan" with no money involved.
    @SerialName("estimated_amount") val estimatedAmount: Double? = null,
    @SerialName("created_by") val createdBy: String,
    val status: String = "open",
    @SerialName("locked_by") val lockedBy: String? = null,
    @SerialName("locked_at") val lockedAt: String? = null,
    @SerialName("converted_expense_id") val convertedExpenseId: String? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class PollVote(
    @SerialName("poll_id") val pollId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("is_in") val isIn: Boolean = false
)

// Not @Serializable — this is a client-side composition of Poll + its votes,
// never decoded directly from a Supabase row.
data class PollWithVotes(
    val poll: Poll,
    val votes: List<PollVote>
) {
    val inCount: Int get() = votes.count { it.isIn }
}