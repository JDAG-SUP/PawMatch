package com.pawmatch.app.shared.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow

class SupabaseAuthDataSource(
    private val supabaseClient: SupabaseClient
) {
    val sessionStatus: Flow<SessionStatus> = supabaseClient.auth.sessionStatus

    suspend fun signInWithEmail(email: String, psw: String): UserInfo? {
        supabaseClient.auth.signInWith(Email) {
            this.email = email
            password = psw
        }
        return supabaseClient.auth.currentUserOrNull()
    }

    suspend fun signUpWithEmail(email: String, psw: String): UserInfo? {
        supabaseClient.auth.signUpWith(Email) {
            this.email = email
            password = psw
        }
        return supabaseClient.auth.currentUserOrNull()
    }

    suspend fun signOut() {
        supabaseClient.auth.signOut()
    }
}
