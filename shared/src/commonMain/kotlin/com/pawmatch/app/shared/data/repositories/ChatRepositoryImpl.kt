package com.pawmatch.app.shared.data.repositories

import com.pawmatch.app.shared.data.remote.SupabaseChatDataSource
import com.pawmatch.app.shared.domain.entities.ChatMessage
import com.pawmatch.app.shared.domain.repositories.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val chatDataSource: SupabaseChatDataSource
) : ChatRepository {

    override suspend fun loadHistory(matchId: String): Result<List<ChatMessage>> = runCatching {
        val myId = chatDataSource.getCurrentUserId() ?: ""
        chatDataSource.loadHistory(matchId).map { dto ->
            ChatMessage(
                id = dto.id ?: "",
                matchId = dto.matchId,
                senderId = dto.senderId,
                content = dto.content,
                createdAt = dto.createdAt ?: "",
                isFromMe = dto.senderId == myId
            )
        }
    }

    override suspend fun sendMessage(matchId: String, content: String): Result<Unit> = runCatching {
        chatDataSource.sendMessage(matchId, content)
    }

    override fun observeChat(matchId: String): Flow<ChatMessage> {
        val myId = chatDataSource.getCurrentUserId() ?: ""
        return chatDataSource.observeChat(matchId).map { dto ->
            ChatMessage(
                id = dto.id ?: "",
                matchId = dto.matchId,
                senderId = dto.senderId,
                content = dto.content,
                createdAt = dto.createdAt ?: "",
                isFromMe = dto.senderId == myId
            )
        }
    }
}
