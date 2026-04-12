package com.pawmatch.app.shared.domain.entities

data class ChatMessage(
    val id: String,
    val matchId: String,
    val senderId: String,
    val content: String,
    val createdAt: String,
    val isFromMe: Boolean
)
