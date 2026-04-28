
package com.pawmatch.app.data.chat

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pawmatch.app.models.Conversation
import com.pawmatch.app.models.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Implementación del repositorio usando Cloud Firestore.
// Centraliza el acceso a las colecciones "chats" y "chats/{id}/messages".
class FirestoreChatRepository(
    // Se inyecta la instancia de Firestore para facilitar reemplazo en tests.
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ChatRepository {

    // Nombres de colecciones en una constante para evitar errores de tipeo.
    private companion object {
        const val CHATS_COLLECTION = "chats"
        const val MESSAGES_COLLECTION = "messages"
    }

    // Escucha en tiempo real las conversaciones del usuario actual.
    // Se filtra con whereArrayContains sobre "participantIds" y se ordena por
    // último mensaje descendente para mostrar los chats más recientes primero.
    override fun observeConversations(currentUserId: String): Flow<List<Conversation>> = callbackFlow {
        val registration = firestore.collection(CHATS_COLLECTION)
            .whereArrayContains("participantIds", currentUserId)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                // Si Firestore reporta un error, cerramos el flujo con la excepción
                // para que el ViewModel pueda manejarlo.
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                // Convertimos cada documento al modelo de dominio.
                val conversations = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)?.copy(id = doc.id)
                }
                trySend(conversations)
            }
        // Al cancelar el flujo, eliminamos el listener para no fugar recursos.
        awaitClose { registration.remove() }
    }

    // Escucha en tiempo real los mensajes de una conversación, ordenados por fecha ascendente
    // (el más antiguo primero) para que la UI los muestre cronológicamente.
    override fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val registration = firestore.collection(CHATS_COLLECTION)
            .document(chatId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                }
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    // Lectura puntual (no en tiempo real) de un chat por ID.
    override suspend fun getConversation(chatId: String): Conversation? {
        val snapshot = firestore.collection(CHATS_COLLECTION)
            .document(chatId)
            .get()
            .await()
        return snapshot.toObject(Conversation::class.java)?.copy(id = snapshot.id)
    }

    // Inserta un nuevo mensaje y actualiza la previsualización del chat padre.
    // Devuelve Result<Unit> para que el ViewModel pueda exponer errores a la UI.
    override suspend fun sendMessage(
        chatId: String,
        senderId: String,
        senderName: String,
        text: String,
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        // Referencia al documento del chat y a un nuevo mensaje con ID auto-generado.
        val chatRef = firestore.collection(CHATS_COLLECTION).document(chatId)
        val messageRef = chatRef.collection(MESSAGES_COLLECTION).document()

        val message = Message(
            id = messageRef.id,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            createdAt = now,
        )

        // Guardamos el mensaje.
        messageRef.set(message).await()

        // Actualizamos los campos de previsualización del chat padre para que
        // la lista de conversaciones se mantenga ordenada y muestre el último texto.
        chatRef.update(
            mapOf(
                "lastMessage" to text,
                "lastMessageAt" to now,
            ),
        ).await()
    }

    // Devuelve el ID del chat existente entre dos usuarios o crea uno nuevo si no existe.
    // Se busca por "participantIds" array-contains uno de los UIDs y se filtra el resto en memoria
    // (Firestore no permite dos array-contains en la misma consulta).
    override suspend fun getOrCreateConversation(
        currentUserId: String,
        currentUserName: String,
        otherUserId: String,
        otherUserName: String,
    ): Result<String> = runCatching {
        // Buscamos chats que ya incluyan al usuario actual.
        val existing = firestore.collection(CHATS_COLLECTION)
            .whereArrayContains("participantIds", currentUserId)
            .get()
            .await()

        // De esos, encontramos el que también contenga al otro usuario.
        val match = existing.documents.firstOrNull { doc ->
            @Suppress("UNCHECKED_CAST")
            val ids = doc.get("participantIds") as? List<String> ?: emptyList()
            ids.contains(otherUserId) && ids.size == 2
        }

        if (match != null) {
            return@runCatching match.id
        }

        // No existe: creamos un nuevo documento con ID auto-generado.
        val now = System.currentTimeMillis()
        val newDocRef = firestore.collection(CHATS_COLLECTION).document()
        val conversation = Conversation(
            id = newDocRef.id,
            participantIds = listOf(currentUserId, otherUserId),
            participantNames = mapOf(
                currentUserId to currentUserName,
                otherUserId to otherUserName,
            ),
            lastMessage = "",
            // Inicializamos lastMessageAt con createdAt para que el chat aparezca
            // inmediatamente en la lista aunque aún no tenga mensajes.
            lastMessageAt = now,
            createdAt = now,
        )
        newDocRef.set(conversation).await()
        newDocRef.id
    }
}