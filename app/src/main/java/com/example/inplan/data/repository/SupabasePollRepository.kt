package com.example.inplan.data.repository



import com.example.inplan.domain.repository.PollRepository
import com.example.inplan.data.model.Poll
import com.example.inplan.data.model.PollVote
import com.example.inplan.data.model.PollWithVotes

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabasePollRepository @Inject constructor(
    private val client: SupabaseClient
) : PollRepository {

    private val userId: String
        get() = client.auth.currentUserOrNull()?.id ?: error("Not signed in")

    override suspend fun createPoll(tripId: String, title: String, estimatedAmount: Double?): Poll =
        client.postgrest.from("polls")
            .insert(
                Poll(tripId = tripId, title = title, estimatedAmount = estimatedAmount, createdBy = userId)
            ) { select() }
            .decodeSingle<Poll>()

    override suspend fun getPolls(tripId: String): List<Poll> =
        client.postgrest.from("polls")
            .select { filter { eq("trip_id", tripId) } }
            .decodeList()

    override suspend fun getPollVotes(pollId: String): List<PollVote> =
        client.postgrest.from("poll_votes")
            .select { filter { eq("poll_id", pollId) } }
            .decodeList()

    override suspend fun setPollVote(pollId: String, isIn: Boolean) {
        client.postgrest.from("poll_votes").upsert(PollVote(pollId = pollId, userId = userId, isIn = isIn))
    }

    override suspend fun lockPoll(pollId: String) {
        client.postgrest.from("polls")
            .update({
                set("status", "locked")
                set("locked_by", userId)
                set("locked_at", Instant.now().toString())
            }) { filter { eq("id", pollId) } }
    }

    override suspend fun cancelPoll(pollId: String) {
        client.postgrest.from("polls")
            .update({ set("status", "cancelled") }) { filter { eq("id", pollId) } }
    }

    override suspend fun markConverted(pollId: String, expenseId: String) {
        client.postgrest.from("polls")
            .update({
                set("status", "converted")
                set("converted_expense_id", expenseId)
            }) { filter { eq("id", pollId) } }
    }

    override fun observePolls(tripId: String): Flow<List<PollWithVotes>> = flow {
        suspend fun snapshot(): List<PollWithVotes> =
            getPolls(tripId).map { poll -> PollWithVotes(poll, getPollVotes(poll.id)) }

        emit(snapshot())

        val pollsChannel = client.channel("polls-$tripId")
        val pollChanges = pollsChannel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "polls" }
        val votesChannel = client.channel("poll-votes-$tripId")
        val voteChanges = votesChannel.postgresChangeFlow<PostgresAction>(schema = "public") { table = "poll_votes" }
        pollsChannel.subscribe()
        votesChannel.subscribe()

        merge(pollChanges, voteChanges).collect { emit(snapshot()) }
    }
}