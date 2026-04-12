package com.pawmatch.app.shared.domain.entities

data class UserSession(
    val id: String,
    val email: String,
    val phone: String? = null,
    val isVerified: Boolean = false
)
