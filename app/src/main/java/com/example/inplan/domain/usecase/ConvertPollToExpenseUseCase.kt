package com.example.inplan.domain.usecase


import com.example.inplan.domain.repository.ExpenseRepository
import com.example.inplan.domain.repository.PollRepository
import com.example.inplan.data.model.Expense

import javax.inject.Inject

/**
 * Converts a locked poll into a real expense.
 *
 * This spans two aggregates (Poll + Expense), which is why it lives here
 * instead of inside either repository. Business rule: if the final amount
 * exceeds the original estimate by more than 20%, the resulting expense is
 * created as "pending_approval" and every included member gets an approval
 * row to sign off on; otherwise it's auto-"confirmed".
 *
 * estimatedAmount is nullable because a poll can be a pure coordination
 * "Plan" with no money attached. The UI never surfaces "Convert to expense"
 * for those in practice, but staying null-safe here means a null estimate is
 * simply never flagged as over-budget rather than throwing.
 */
class ConvertPollToExpenseUseCase @Inject constructor(
    private val pollRepository: PollRepository,
    private val expenseRepository: ExpenseRepository
) {
    companion object {
        private const val OVER_BUDGET_MULTIPLIER = 1.2
    }

    suspend operator fun invoke(
        pollId: String,
        tripId: String,
        title: String,
        category: String,
        finalAmount: Double,
        estimatedAmount: Double?,
        paidByUserId: String
    ): Expense {
        val votes = pollRepository.getPollVotes(pollId)
        val inUserIds = votes.filter { it.isIn }.map { it.userId }
        require(inUserIds.isNotEmpty()) { "No one voted in on this poll" }

        val isOverBudget = estimatedAmount != null && finalAmount > estimatedAmount * OVER_BUDGET_MULTIPLIER

        val expense = expenseRepository.createExpenseFromPoll(
            tripId = tripId,
            title = title,
            category = category,
            finalAmount = finalAmount,
            paidByUserId = paidByUserId,
            sourcePollId = pollId,
            splitAmongUserIds = inUserIds,
            needsApproval = isOverBudget
        )

        pollRepository.markConverted(pollId, expense.id)
        return expense
    }
}