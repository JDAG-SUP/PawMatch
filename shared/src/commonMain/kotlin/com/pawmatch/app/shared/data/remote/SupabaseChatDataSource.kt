package com.pawmatch.app.shared.data.remote

import com.pawmatch.app.shared.data.remote.dto.MessageDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class SupabaseChatDataSource(
    private val supabaseClient: SupabaseClient
) {

    fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    suspend fun loadHistory(matchId: String): List<MessageDto> {
        return supabaseClient.postgrest["messages"]
            .select { 
                filter { eq("match_id", matchId) }
                // order("created_at", Order.ASCENDING) omitido por simplicidad MVP (se asume default sort o se ordena en repo)
            }
            .decodeList<MessageDto>()
            .sortedBy { it.createdAt }
    }

    suspend fun sendMessage(matchId: String, content: String) {
        val userId = getCurrentUserId() ?: throw Exception("Unauthorized")
        
        val dto = MessageDto(
            matchId = matchId,
            senderId = userId,
            content = content
        )
        supabaseClient.postgrest["messages"].insert(dto)
    }

    fun observeChat(matchId: String): Flow<MessageDto> {
        val channel = supabaseClient.channel("public:messages:match_$matchId")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "messages"
            filter = "match_id=eq.$matchId"
        }
        
        return changeFlow.mapNotNull { action ->
            action.decodeRecord<MessageDto>()
        }
    }
}
