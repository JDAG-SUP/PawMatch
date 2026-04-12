package com.pawmatch.app.shared.presentation.viewmodels

import com.pawmatch.app.shared.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<AuthState>(AuthState.Idle)
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { session ->
                if (session != null) {
                    _uiState.update { AuthState.Authenticated(session) }
                } else {
                    _uiState.update { AuthState.Unauthenticated }
                }
            }
        }
    }

    fun signIn(email: String, psw: String) {
        viewModelScope.launch {
            _uiState.update { AuthState.Loading }
            val result = authRepository.signInWithEmail(email, psw)
            result.onFailure { error ->
                _uiState.update { AuthState.Error(error.message ?: "SignIn Error") }
            }
        }
    }

    fun signUp(email: String, psw: String) {
        viewModelScope.launch {
            _uiState.update { AuthState.Loading }
            val result = authRepository.signUpWithEmail(email, psw)
            result.onFailure { error ->
                _uiState.update { AuthState.Error(error.message ?: "SignUp Error") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
