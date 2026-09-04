package com.example.inplan.domain.repository


import com.example.inplan.data.model.Expense
import com.example.inplan.data.model.TripBalance
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(
        tripId: String,
        title: String,
        category: String,
        amount: Double,
        taxAmount: Double,
        splitAmongUserIds: List<String>
    )

    suspend fun getExpenses(tripId: String): List<Expense>
    fun observeExpenses(tripId: String): Flow<List<Expense>>
    suspend fun getBalances(tripId: String): List<TripBalance>
    suspend fun markSplitSettled(splitId: String)

    /**
     * Used by ConvertPollToExpenseUseCase — inserts an expense that originates
     * from a poll (plus even splits, plus approval rows if [needsApproval] is
     * true). The over-budget decision itself lives in the use case, not here.
     */
    suspend fun createExpenseFromPoll(
        tripId: String,
        title: String,
        category: String,
        finalAmount: Double,
        paidByUserId: String,
        sourcePollId: String,
        splitAmongUserIds: List<String>,
        needsApproval: Boolean
    ): Expense
}