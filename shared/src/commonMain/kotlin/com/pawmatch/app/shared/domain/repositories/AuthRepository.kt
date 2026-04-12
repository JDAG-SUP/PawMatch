package com.pawmatch.app.shared.domain.repositories

import com.pawmatch.app.shared.domain.entities.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authState: Flow<UserSession?>
    
    suspend fun signInWithEmail(email: String, password: String): Result<UserSession>
    
    suspend fun signUpWithEmail(email: String, password: String): Result<UserSession>
    
    suspend fun signOut(): Result<Unit>
}
