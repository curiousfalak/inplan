package com.example.inplan.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// No standalone "id" column — keyed on (expense_id, user_id).
@Serializable
data class ExpenseApproval(
    @SerialName("expense_id") val expenseId: String,
    @SerialName("user_id") val userId: String,
    val approved: Boolean = false,
    @SerialName("responded_at") val respondedAt: String? = null
)
