package com.example.inplan.data.model



import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id: String = "",
    @SerialName("trip_id") val tripId: String,
    @SerialName("paid_by") val paidBy: String,
    val title: String,
    // Nullable — category is optional in the schema.
    val category: String? = null,
    val amount: Double,
    @SerialName("tax_amount") val taxAmount: Double? = 0.0,
    @SerialName("created_at") val createdAt: String = "",
    val notes: String? = null,
    @SerialName("source_poll_id") val sourcePollId: String? = null,
    val status: String = "confirmed"
)

@Serializable
data class ExpenseSplit(
    val id: String = "",
    @SerialName("expense_id") val expenseId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("share_amount") val shareAmount: Double,
    @SerialName("is_settled") val isSettled: Boolean = false,
    @SerialName("settled_at") val settledAt: String? = null
)