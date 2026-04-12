package com.pawmatch.app.shared.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    @SerialName("id") val id: String? = null,
    @SerialName("match_id") val matchId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("content") val content: String,
    @SerialName("type") val type: String = "text",
    @SerialName("created_at") val createdAt: String? = null
)
