package com.pawmatch.app.shared.data.repositories

import com.pawmatch.app.shared.data.remote.SupabaseAuthDataSource
import com.pawmatch.app.shared.domain.entities.UserSession
import com.pawmatch.app.shared.domain.repositories.AuthRepository
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authDataSource: SupabaseAuthDataSource
) : AuthRepository {

    override val authState: Flow<UserSession?> = authDataSource.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Authenticated -> {
                val user = status.session.user
                if (user != null) {
                    UserSession(
                        id = user.id,
                        email = user.email ?: "",
                        phone = user.phone,
                        isVerified = user.emailConfirmedAt != null || user.phoneConfirmedAt != null
                    )
                } else null
            }
            else -> null
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<UserSession> {
        return try {
            val user = authDataSource.signInWithEmail(email, password)
            if (user != null) {
                Result.success(
                    UserSession(
                        id = user.id,
                        email = user.email ?: "",
                        phone = user.phone,
                        isVerified = user.emailConfirmedAt != null || user.phoneConfirmedAt != null
                    )
                )
            } else {
                Result.failure(Exception("Could not retrieve user info after sign-in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<UserSession> {
        return try {
            val user = authDataSource.signUpWithEmail(email, password)
            if (user != null) {
                Result.success(
                    UserSession(
                        id = user.id,
                        email = user.email ?: "",
                        phone = user.phone,
                        isVerified = user.emailConfirmedAt != null || user.phoneConfirmedAt != null
                    )
                )
            } else {
                Result.failure(Exception("Registration succeeded but no user info retrieved"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            authDataSource.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
