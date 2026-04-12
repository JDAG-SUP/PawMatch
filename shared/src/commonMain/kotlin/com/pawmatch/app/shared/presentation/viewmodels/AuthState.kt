package com.pawmatch.app.shared.presentation.viewmodels

import com.pawmatch.app.shared.domain.entities.UserSession

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val session: UserSession) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
