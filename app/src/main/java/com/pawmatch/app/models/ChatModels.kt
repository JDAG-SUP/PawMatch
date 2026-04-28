/*
 * Copyright 2026 Vincent Tsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.pawmatch.app.models

// Modelo que representa una conversación entre dos usuarios.
// Se almacena en la colección "chats/{chatId}" en Firestore.
data class Conversation(
    // Identificador único del chat (coincide con el ID del documento en Firestore).
    val id: String = "",
    // Lista con los UIDs de los participantes (siempre dos en chat 1-a-1).
    // Se usa para filtrar conversaciones del usuario actual con whereArrayContains.
    val participantIds: List<String> = emptyList(),
    // Mapa UID -> nombre visible. Permite mostrar el nombre del otro usuario
    // sin tener que leer la colección de usuarios cada vez.
    val participantNames: Map<String, String> = emptyMap(),
    // Texto del último mensaje (para previsualizar en la lista de chats).
    val lastMessage: String = "",
    // Marca de tiempo del último mensaje en milisegundos epoch.
    // Se usa para ordenar la lista de chats de más reciente a más antiguo.
    val lastMessageAt: Long = 0L,
    // Marca de tiempo de creación del chat en milisegundos epoch.
    val createdAt: Long = 0L,
)

// Modelo que representa un mensaje individual dentro de una conversación.
// Se almacena en la subcolección "chats/{chatId}/messages/{messageId}".
data class Message(
    // Identificador único del mensaje (coincide con el ID del documento).
    val id: String = "",
    // ID del chat al que pertenece el mensaje (denormalizado para depuración).
    val chatId: String = "",
    // UID del usuario que envió el mensaje.
    val senderId: String = "",
    // Nombre del remitente al momento del envío (denormalizado).
    val senderName: String = "",
    // Contenido textual del mensaje.
    val text: String = "",
    // Marca de tiempo de envío en milisegundos epoch.
    // Se usa para ordenar los mensajes cronológicamente.
    val createdAt: Long = 0L,
)