package com.example.inplan.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inplan.data.model.Expense
import com.example.inplan.data.model.PollWithVotes
import com.example.inplan.data.model.Profile
import com.example.inplan.data.model.Trip
import com.example.inplan.data.model.TripBalance
import com.example.inplan.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    val currentUserId: String?
        get() = tripRepository.currentUserIdOrNull

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    fun loadTrips() = viewModelScope.launch {
        try {
            val fresh = tripRepository.myTrips()
            _trips.value = (fresh + _trips.value).distinctBy { it.id }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to load trips", e)
            _errorMessage.value = "Couldn't load your trips."
        }
    }

    fun watchTrips() = viewModelScope.launch {
        try {
            tripRepository.observeTrips().collect { list ->
                _trips.value = (list + _trips.value).distinctBy { it.id }
            }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to watch trips", e)
            _errorMessage.value = "Couldn't load your trips."
        }
    }

    fun createTrip(name: String, onCreated: (Trip) -> Unit) = viewModelScope.launch {
        try {
            val trip = tripRepository.createTrip(name)
            _trips.value = _trips.value + trip
            onCreated(trip)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to create trip", e)
            _errorMessage.value = "Couldn't create trip. Please try again."
        }
    }

    fun joinTripByCode(code: String, onJoined: (Trip) -> Unit) = viewModelScope.launch {
        try {
            val trip = tripRepository.joinTripByCode(code)
            onJoined(trip)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to join trip by code", e)
            _errorMessage.value = e.message ?: "Couldn't join trip. Check the code and try again."
        }
    }

    fun joinTrip(tripId: String, onJoined: () -> Unit) = viewModelScope.launch {
        try {
            tripRepository.joinTrip(tripId)
            onJoined()
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to join trip", e)
            _errorMessage.value = e.message ?: "Couldn't join this trip. The link may be invalid."
        }
    }

    private val _currentTrip = MutableStateFlow<Trip?>(null)
    val currentTrip: StateFlow<Trip?> = _currentTrip.asStateFlow()

    fun watchTrip(tripId: String) = viewModelScope.launch {
        try {
            tripRepository.observeTrip(tripId).collect { trip ->
                _currentTrip.value = trip
            }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to watch trip", e)
        }
    }

    fun loadTrip(tripId: String) = viewModelScope.launch {
        try {
            _currentTrip.value = tripRepository.getTrip(tripId)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to load trip", e)
            _errorMessage.value = "Couldn't load trip details."
        }
    }

    private fun checkAndUpdateTripStatus(tripId: String) = viewModelScope.launch {
        try {
            val splits = tripRepository.getExpenseSplitsForTrip(tripId)
            val allSettled = splits.isNotEmpty() && splits.all { it.isSettled }
            tripRepository.updateTripStatus(tripId, if (allSettled) "settled" else "active")
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to update trip status", e)
        }
    }

    private val _tripMembers = MutableStateFlow<List<String>>(emptyList())
    val tripMembers: StateFlow<List<String>> = _tripMembers.asStateFlow()

    fun watchTripMembers(tripId: String) = viewModelScope.launch {
        try {
            tripRepository.observeTripMembers(tripId).collect { ids ->
                _tripMembers.value = ids
            }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to watch trip members", e)
        }
    }

    private val _organizerProfile = MutableStateFlow<Profile?>(null)
    val organizerProfile: StateFlow<Profile?> = _organizerProfile.asStateFlow()

    private val _profileNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val profileNames: StateFlow<Map<String, String>> = _profileNames.asStateFlow()

    private val _balanceProfiles = MutableStateFlow<Map<String, Profile>>(emptyMap())
    val balanceProfiles: StateFlow<Map<String, Profile>> = _balanceProfiles.asStateFlow()

    fun loadBalanceProfiles(userIds: List<String>) = viewModelScope.launch {
        try {
            val profiles = tripRepository.getProfiles(userIds.distinct())
            _balanceProfiles.value = profiles.associateBy { it.id }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to load balance profiles", e)
        }
    }

    fun loadOrganizerProfile(userId: String) = viewModelScope.launch {
        try {
            _organizerProfile.value = tripRepository.getProfiles(listOf(userId)).firstOrNull()
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to load organizer profile", e)
        }
    }

    fun loadProfileNames(userIds: List<String>) = viewModelScope.launch {
        try {
            val profiles = tripRepository.getProfiles(userIds)
            _profileNames.value = profiles.associate { it.id to it.fullName }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to load profile names", e)
        }
    }

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _balances = MutableStateFlow<List<TripBalance>>(emptyList())
    val balances: StateFlow<List<TripBalance>> = _balances.asStateFlow()

    fun watchExpenses(tripId: String) = viewModelScope.launch {
        try {
            tripRepository.observeExpenses(tripId).collect { list ->
                _expenses.value = list
                loadBalances(tripId)
            }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to watch expenses", e)
        }
    }

    fun loadBalances(tripId: String) = viewModelScope.launch {
        try {
            _balances.value = tripRepository.getBalances(tripId)
            checkAndUpdateTripStatus(tripId)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to load balances", e)
            _errorMessage.value = "Couldn't load balances."
        }
    }

    fun addExpense(
        tripId: String,
        title: String,
        category: String,
        amount: Double,
        tax: Double,
        splitAmong: List<String>
    ) = viewModelScope.launch {
        try {
            tripRepository.addExpense(
                tripId = tripId,
                title = title,
                category = category,
                amount = amount,
                taxAmount = tax,
                splitAmongUserIds = splitAmong
            )
            loadBalances(tripId)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to add expense", e)
            _errorMessage.value = "Couldn't add expense. Please try again."
        }
    }

    fun settleUp(tripId: String, onResult: (success: Boolean) -> Unit = {}) = viewModelScope.launch {
        try {
            tripRepository.markSplitsSettledForUser(tripId)
            loadBalances(tripId)
            onResult(true)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to settle up", e)
            _errorMessage.value = e.message ?: "Couldn't mark as settled. Please try again."
            onResult(false)
        }
    }

    private val _polls = MutableStateFlow<List<PollWithVotes>>(emptyList())
    val polls: StateFlow<List<PollWithVotes>> = _polls.asStateFlow()

    fun watchPolls(tripId: String) = viewModelScope.launch {
        try {
            tripRepository.observePolls(tripId).collect { list ->
                _polls.value = list
            }
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to watch polls", e)
        }
    }

    fun refreshPolls(tripId: String) = viewModelScope.launch {
        try {
            _polls.value = tripRepository.getPollsWithVotes(tripId)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to refresh polls", e)
        }
    }

    fun createPoll(tripId: String, title: String, estimatedAmount: Double?) = viewModelScope.launch {
        try {
            tripRepository.createPoll(tripId = tripId, title = title, estimatedAmount = estimatedAmount)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to create poll", e)
            _errorMessage.value = "Couldn't create poll. Please try again."
        }
    }

    fun votePoll(tripId: String, pollId: String, isIn: Boolean) = viewModelScope.launch {
        try {
            tripRepository.setPollVote(pollId, isIn)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to vote on poll", e)
        }
    }

    fun lockPoll(tripId: String, pollId: String) = viewModelScope.launch {
        try {
            tripRepository.lockPoll(pollId)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to lock poll", e)
        }
    }

    fun cancelPoll(tripId: String, pollId: String) = viewModelScope.launch {
        try {
            tripRepository.cancelPoll(pollId)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to cancel poll", e)
        }
    }

    fun convertPoll(
        pollId: String,
        tripId: String,
        title: String,
        category: String,
        finalAmount: Double,
        estimatedAmount: Double?,
        paidByUserId: String
    ) = viewModelScope.launch {
        try {
            tripRepository.convertPollToExpense(
                pollId = pollId,
                tripId = tripId,
                title = title,
                category = category,
                finalAmount = finalAmount,
                estimatedAmount = estimatedAmount,
                paidByUserId = paidByUserId
            )
            loadBalances(tripId)
        } catch (e: Exception) {
            Log.e("TripViewModel", "Failed to convert poll", e)
            _errorMessage.value = "Couldn't convert poll to expense."
        }
    }

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }
}