package com.example.inplan.data.repository

import com.example.inplan.data.model.Expense
import com.example.inplan.data.model.ExpenseSplit
import com.example.inplan.data.model.Poll
import com.example.inplan.data.model.PollVote
import com.example.inplan.data.model.PollWithVotes
import com.example.inplan.data.model.Profile
import com.example.inplan.data.model.Trip
import com.example.inplan.data.model.TripBalance
import com.example.inplan.data.model.TripIdParam
import com.example.inplan.data.model.TripMember
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(private val client: SupabaseClient) {

    private val userId: String
        get() = client.auth.currentUserOrNull()?.id ?: error("Not signed in")

    open val currentUserIdOrNull: String?
        get() = client.auth.currentUserOrNull()?.id

    // ---------- Trips ----------

    suspend fun createTrip(name: String): Trip {
        val trip = client.postgrest.from("trips")
            .insert(
                Trip(name = name, createdBy = userId, inviteCode = generateInviteCode())
            ) { select() }
            .decodeSingle<Trip>()
        joinTrip(trip.id)
        return trip
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    suspend fun joinTrip(tripId: String) {
        try {
            client.postgrest.from("trip_members").insert(
                TripMember(tripId = tripId, userId = userId)
            )
        } catch (e: Exception) {
            val alreadyMember = getTripMembers(tripId).contains(userId)
            if (!alreadyMember) throw e
        }
    }

    suspend fun joinTripByCode(code: String): Trip {
        val trip = client.postgrest.from("trips")
            .select { filter { eq("invite_code", code.trim().uppercase()) } }
            .decodeSingleOrNull<Trip>()
            ?: error("No trip found with that code")
        joinTrip(trip.id)
        return trip
    }

    open suspend fun myTrips(): List<Trip> =
        client.postgrest.from("trips").select().decodeList()

    fun observeTrips(): Flow<List<Trip>> = flow {
        emit(myTrips())
        val channel = client.channel("my-trips")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "trips"
        }
        channel.subscribe()
        changes.collect { emit(myTrips()) }
    }

    suspend fun getTrip(tripId: String): Trip =
        client.postgrest.from("trips")
            .select { filter { eq("id", tripId) } }
            .decodeSingle()

    fun observeTrip(tripId: String): Flow<Trip> = flow {
        emit(getTrip(tripId))
        val channel = client.channel("trip-$tripId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "trips"
        }
        channel.subscribe()
        changes.collect { emit(getTrip(tripId)) }
    }

    // ---------- Trip members ----------

    suspend fun getTripMembers(tripId: String): List<String> =
        client.postgrest.from("trip_members")
            .select { filter { eq("trip_id", tripId) } }
            .decodeList<TripMember>()
            .map { it.userId }

    fun observeTripMembers(tripId: String): Flow<List<String>> = flow {
        emit(getTripMembers(tripId))
        val channel = client.channel("trip-members-$tripId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "trip_members"
        }
        channel.subscribe()
        changes.collect { emit(getTripMembers(tripId)) }
    }

    // ---------- Expenses ----------

    suspend fun addExpense(
        tripId: String,
        title: String,
        category: String,
        amount: Double,
        taxAmount: Double,
        splitAmongUserIds: List<String>
    ) {
        require(splitAmongUserIds.isNotEmpty()) { "Cannot add an expense with no one to split it among" }

        val expense = client.postgrest.from("expenses")
            .insert(
                Expense(
                    tripId = tripId,
                    paidBy = userId,
                    title = title,
                    category = category,
                    amount = amount,
                    taxAmount = taxAmount
                )
            ) { select() }
            .decodeSingle<Expense>()

        val perPersonShare = (amount + taxAmount) / splitAmongUserIds.size
        val splits = splitAmongUserIds.map {
            ExpenseSplit(expenseId = expense.id, userId = it, shareAmount = perPersonShare)
        }
        client.postgrest.from("expense_splits").insert(splits)
    }

    suspend fun getExpenses(tripId: String): List<Expense> =
        client.postgrest.from("expenses")
            .select { filter { eq("trip_id", tripId) } }
            .decodeList()

    fun observeExpenses(tripId: String): Flow<List<Expense>> = flow {
        emit(getExpenses(tripId))
        val channel = client.channel("trip-expenses-$tripId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "expenses"
        }
        channel.subscribe()
        changes.collect { emit(getExpenses(tripId)) }
    }

    // ---------- Settlement ----------

    suspend fun getBalances(tripId: String): List<TripBalance> =
        client.postgrest.rpc(
            "get_trip_balances",
            TripIdParam(tripId = tripId)
        ).decodeList()

    suspend fun markSplitSettled(splitId: String) {
        @kotlinx.serialization.Serializable
        data class IdOnly(val id: String)

        val updatedRows = client.postgrest.from("expense_splits")
            .update({ set("is_settled", true) }) {
                filter { eq("id", splitId) }
                select(columns = Columns.list("id"))
            }
            .decodeList<IdOnly>()

        if (updatedRows.isEmpty()) {
            error(
                "Couldn't mark this settled — no matching split was updated. " +
                        "This is almost always a Supabase Row Level Security policy " +
                        "missing an UPDATE rule for expense_splits (auth.uid() = user_id)."
            )
        }
    }

    // ---------- Polls ----------

    suspend fun createPoll(tripId: String, title: String, estimatedAmount: Double?): Poll =
        client.postgrest.from("polls")
            .insert(
                Poll(
                    tripId = tripId,
                    title = title,
                    estimatedAmount = estimatedAmount,
                    createdBy = userId
                )
            ) { select() }
            .decodeSingle<Poll>()

    suspend fun getPolls(tripId: String): List<Poll> =
        client.postgrest.from("polls")
            .select { filter { eq("trip_id", tripId) } }
            .decodeList()

    suspend fun getPollVotes(pollId: String): List<PollVote> =
        client.postgrest.from("poll_votes")
            .select { filter { eq("poll_id", pollId) } }
            .decodeList()

    suspend fun getPollsWithVotes(tripId: String): List<PollWithVotes> =
        getPolls(tripId).map { poll -> PollWithVotes(poll, getPollVotes(poll.id)) }

    suspend fun setPollVote(pollId: String, isIn: Boolean) {
        client.postgrest.from("poll_votes").upsert(
            PollVote(pollId = pollId, userId = userId, isIn = isIn)
        )
    }

    suspend fun lockPoll(pollId: String) {
        client.postgrest.from("polls")
            .update({
                set("status", "locked")
                set("locked_by", userId)
                set("locked_at", java.time.Instant.now().toString())
            }) { filter { eq("id", pollId) } }
    }

    suspend fun cancelPoll(pollId: String) {
        client.postgrest.from("polls")
            .update({ set("status", "cancelled") }) { filter { eq("id", pollId) } }
    }

    suspend fun convertPollToExpense(
        pollId: String,
        tripId: String,
        title: String,
        category: String,
        finalAmount: Double,
        estimatedAmount: Double?,
        paidByUserId: String
    ) {
        val votes = getPollVotes(pollId)
        val inUserIds = votes.filter { it.isIn }.map { it.userId }
        require(inUserIds.isNotEmpty()) { "No one voted in on this poll" }

        val isOverBudget = estimatedAmount != null && finalAmount > estimatedAmount * 1.2
        val status = if (isOverBudget) "pending_approval" else "confirmed"

        val expense = client.postgrest.from("expenses")
            .insert(
                buildJsonObject {
                    put("trip_id", tripId)
                    put("paid_by", paidByUserId)
                    put("title", title)
                    put("category", category)
                    put("amount", finalAmount)
                    put("tax_amount", 0.0)
                    put("source_poll_id", pollId)
                    put("status", status)
                }
            ) { select() }
            .decodeSingle<Expense>()

        val perPersonShare = finalAmount / inUserIds.size
        val splits = inUserIds.map {
            ExpenseSplit(expenseId = expense.id, userId = it, shareAmount = perPersonShare)
        }
        client.postgrest.from("expense_splits").insert(splits)

        if (isOverBudget) {
            val approvals = inUserIds.map {
                buildJsonObject {
                    put("expense_id", expense.id)
                    put("user_id", it)
                    put("approved", false)
                }
            }
            client.postgrest.from("expense_approvals").insert(approvals)
        }

        client.postgrest.from("polls")
            .update({
                set("status", "converted")
                set("converted_expense_id", expense.id)
            }) { filter { eq("id", pollId) } }
    }

    fun observePolls(tripId: String): Flow<List<PollWithVotes>> = flow {
        emit(getPollsWithVotes(tripId))

        val pollsChannel = client.channel("polls-$tripId")
        val pollChanges = pollsChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "polls"
        }
        val votesChannel = client.channel("poll-votes-$tripId")
        val voteChanges = votesChannel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "poll_votes"
        }
        pollsChannel.subscribe()
        votesChannel.subscribe()

        merge(pollChanges, voteChanges).collect {
            emit(getPollsWithVotes(tripId))
        }
    }

    // ---------- Profiles ----------

    suspend fun getProfiles(userIds: List<String>): List<Profile> =
        client.postgrest.from("profiles")
            .select { filter { isIn("id", userIds) } }
            .decodeList()

    // ---------- Settlement (split-level) ----------

    suspend fun getExpenseSplitsForTrip(tripId: String): List<ExpenseSplit> {
        @kotlinx.serialization.Serializable
        data class ExpenseIdOnly(val id: String)

        val expenseIds = client.postgrest.from("expenses")
            .select(columns = Columns.list("id")) { filter { eq("trip_id", tripId) } }
            .decodeList<ExpenseIdOnly>()
            .map { it.id }

        if (expenseIds.isEmpty()) return emptyList()

        return client.postgrest.from("expense_splits")
            .select { filter { isIn("expense_id", expenseIds) } }
            .decodeList()
    }

    suspend fun markSplitsSettledForUser(tripId: String) {
        val unsettled = getExpenseSplitsForTrip(tripId).filter { it.userId == userId && !it.isSettled }
        unsettled.forEach { split -> markSplitSettled(split.id) }
    }

    suspend fun updateTripStatus(tripId: String, status: String) {
        client.postgrest.from("trips")
            .update({ set("status", status) }) { filter { eq("id", tripId) } }
    }
}