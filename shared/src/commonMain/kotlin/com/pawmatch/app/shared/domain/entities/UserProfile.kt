package com.pawmatch.app.shared.domain.entities

data class UserProfile(
    val id: String,
    val displayName: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null,
    val isVerified: Boolean = false
)
