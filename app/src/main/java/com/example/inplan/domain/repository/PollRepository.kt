package com.example.inplan.domain.repository

import com.example.inplan.data.model.Poll
import com.example.inplan.data.model.PollVote
import com.example.inplan.data.model.PollWithVotes
import kotlinx.coroutines.flow.Flow

interface PollRepository {
    suspend fun createPoll(tripId: String, title: String, estimatedAmount: Double?): Poll
    suspend fun getPolls(tripId: String): List<Poll>
    suspend fun getPollVotes(pollId: String): List<PollVote>
    suspend fun setPollVote(pollId: String, isIn: Boolean)
    suspend fun lockPoll(pollId: String)
    suspend fun cancelPoll(pollId: String)
    suspend fun markConverted(pollId: String, expenseId: String)
    fun observePolls(tripId: String): Flow<List<PollWithVotes>>
}