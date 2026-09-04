package com.example.inplan.data.repository



import com.example.inplan.domain.repository.ExpenseRepository
import com.example.inplan.data.model.Expense
import com.example.inplan.data.model.ExpenseApproval
import com.example.inplan.data.model.ExpenseSplit
import com.example.inplan.data.model.TripBalance
import com.example.inplan.data.model.TripIdParam

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseExpenseRepository @Inject constructor(
    private val client: SupabaseClient
) : ExpenseRepository {

    private val userId: String
        get() = client.auth.currentUserOrNull()?.id ?: error("Not signed in")

    override suspend fun addExpense(
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

        insertEvenSplits(expense.id, amount + taxAmount, splitAmongUserIds)
    }

    override suspend fun getExpenses(tripId: String): List<Expense> =
        client.postgrest.from("expenses")
            .select { filter { eq("trip_id", tripId) } }
            .decodeList()

    override fun observeExpenses(tripId: String): Flow<List<Expense>> = flow {
        emit(getExpenses(tripId))
        val channel = client.channel("trip-expenses-$tripId")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "expenses"
        }
        channel.subscribe()
        changes.collect { emit(getExpenses(tripId)) }
    }

    override suspend fun getBalances(tripId: String): List<TripBalance> =
        client.postgrest.rpc("get_trip_balances", TripIdParam(tripId = tripId)).decodeList()

    override suspend fun markSplitSettled(splitId: String) {
        client.postgrest.from("expense_splits")
            .update({ set("is_settled", true) }) { filter { eq("id", splitId) } }
    }

    override suspend fun createExpenseFromPoll(
        tripId: String,
        title: String,
        category: String,
        finalAmount: Double,
        paidByUserId: String,
        sourcePollId: String,
        splitAmongUserIds: List<String>,
        needsApproval: Boolean
    ): Expense {
        val status = if (needsApproval) "pending_approval" else "confirmed"

        val expense = client.postgrest.from("expenses")
            .insert(
                Expense(
                    tripId = tripId,
                    paidBy = paidByUserId,
                    title = title,
                    category = category,
                    amount = finalAmount,
                    taxAmount = 0.0,
                    sourcePollId = sourcePollId,
                    status = status
                )
            ) { select() }
            .decodeSingle<Expense>()

        insertEvenSplits(expense.id, finalAmount, splitAmongUserIds)

        if (needsApproval) {
            val approvals = splitAmongUserIds.map {
                ExpenseApproval(expenseId = expense.id, userId = it)
            }
            client.postgrest.from("expense_approvals").insert(approvals)
        }

        return expense
    }

    // Shared by addExpense() and createExpenseFromPoll() — was duplicated
    // inline in both spots in the original TripRepository.
    private suspend fun insertEvenSplits(expenseId: String, totalAmount: Double, userIds: List<String>) {
        val perPersonShare = totalAmount / userIds.size
        val splits = userIds.map {
            ExpenseSplit(expenseId = expenseId, userId = it, shareAmount = perPersonShare)
        }
        client.postgrest.from("expense_splits").insert(splits)
    }
}