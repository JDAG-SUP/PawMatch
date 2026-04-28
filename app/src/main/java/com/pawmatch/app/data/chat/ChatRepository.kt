
package com.pawmatch.app.data.chat

import com.pawmatch.app.models.Conversation
import com.pawmatch.app.models.Message
import kotlinx.coroutines.flow.Flow

// Contrato del repositorio de chat.
// Se define como interfaz para poder cambiar la implementación (Firestore, fake en tests, etc.)
// sin afectar a los ViewModels ni a la UI.
interface ChatRepository {

    // Observa en tiempo real la lista de conversaciones del usuario indicado.
    // Devuelve un Flow que emite cada vez que cambian los datos en Firestore.
    fun observeConversations(currentUserId: String): Flow<List<Conversation>>

    // Observa en tiempo real los mensajes de una conversación, ordenados por fecha ascendente.
    fun observeMessages(chatId: String): Flow<List<Message>>

    // Obtiene una conversación puntual por su ID (útil para encabezados, etc.).
    suspend fun getConversation(chatId: String): Conversation?

    // Envía un nuevo mensaje a la conversación indicada.
    // También actualiza los campos de previsualización (lastMessage, lastMessageAt) del chat.
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        text: String,
    ): Result<Unit>

    // Crea (si no existe) o devuelve el ID de la conversación entre dos usuarios.
    // Se usará posteriormente para abrir un chat desde la pantalla de Matches.
    suspend fun getOrCreateConversation(
        currentUserId: String,
        currentUserName: String,
        otherUserId: String,
        otherUserName: String,
    ): Result<String>
}