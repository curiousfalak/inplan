package com.example.inplan.viewmodel

import com.example.inplan.data.repository.AuthRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    val isSignedIn: Boolean get() = repo.currentUserId != null

    private val _needsProfileSetup = MutableStateFlow(true)
    val needsProfileSetup: StateFlow<Boolean> = _needsProfileSetup.asStateFlow()

    fun signUp(email: String, password: String, fullName: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching { repo.signUp(email, password, fullName) }
            .onSuccess {
                _needsProfileSetup.value = true
                _state.value = AuthState.Success
            }
            .onFailure { _state.value = AuthState.Error(friendlyAuthError(it)) }
    }

    fun signOut(onDone: () -> Unit) = viewModelScope.launch {
        runCatching { repo.signOut() }
        // Fire onDone regardless of success — even if the network call to
        // Supabase failed, we still want to route the user back to the
        // sign-in screen locally rather than leaving them stuck.
        onDone()
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState.Loading
        runCatching { repo.signIn(email, password) }
            .onSuccess {
                _needsProfileSetup.value = !repo.hasUpiId()
                _state.value = AuthState.Success
            }
            .onFailure { _state.value = AuthState.Error(friendlyAuthError(it)) }
    }

    private val _currentUpiId = MutableStateFlow<String?>(null)
    val currentUpiId: StateFlow<String?> = _currentUpiId.asStateFlow()

    fun loadCurrentUpiId() = viewModelScope.launch {
        _currentUpiId.value = runCatching { repo.getMyUpiId() }.getOrNull()
    }

    private val _upiUpdateState = MutableStateFlow<AuthState>(AuthState.Idle)
    val upiUpdateState: StateFlow<AuthState> = _upiUpdateState.asStateFlow()

    fun updateUpiId(upiId: String) = viewModelScope.launch {
        _upiUpdateState.value = AuthState.Loading
        runCatching { repo.updateUpiId(upiId) }
            .onSuccess {
                _currentUpiId.value = upiId
                _upiUpdateState.value = AuthState.Success
            }
            .onFailure { _upiUpdateState.value = AuthState.Error(friendlyAuthError(it)) }
    }

    private fun friendlyAuthError(e: Throwable): String {
        val msg = e.message.orEmpty()
        return when {
            msg.contains("Row Level Security", ignoreCase = true) ->
                msg

            e is java.net.UnknownHostException ||
                    msg.contains("Unable to resolve host", ignoreCase = true) ||
                    msg.contains("No address associated with hostname", ignoreCase = true) ->
                "No internet connection. Please check your network and try again."

            e is java.net.SocketTimeoutException || msg.contains("timeout", ignoreCase = true) ->
                "The request timed out. Please check your connection and try again."

            msg.contains("Invalid login credentials", ignoreCase = true) ->
                "Incorrect email or password."

            msg.contains("Email not confirmed", ignoreCase = true) ->
                "Please verify your email before signing in."

            msg.contains("User already registered", ignoreCase = true) ->
                "An account with this email already exists — try signing in instead."

            msg.contains("Password should be at least", ignoreCase = true) ->
                "Password is too short — please use a longer one."

            msg.contains("Unable to validate email", ignoreCase = true) ||
                    msg.contains("invalid email", ignoreCase = true) ->
                "That doesn't look like a valid email address."

            else -> "Something went wrong. Please try again."
        }
    }
}