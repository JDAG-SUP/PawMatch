package com.pawmatch.app.shared.domain.repositories

import com.pawmatch.app.shared.domain.entities.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun loadHistory(matchId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(matchId: String, content: String): Result<Unit>
    fun observeChat(matchId: String): Flow<ChatMessage>
}
