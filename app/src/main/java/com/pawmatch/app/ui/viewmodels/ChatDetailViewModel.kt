/*
 * Copyright 2026 Vincent Tsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.pawmatch.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.pawmatch.app.data.chat.ChatRepository
import com.pawmatch.app.data.chat.FirestoreChatRepository
import com.pawmatch.app.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel responsable de exponer los mensajes de una conversación específica
// y permitir enviar nuevos mensajes. Toda la interacción con Firestore queda
// encapsulada en el repositorio inyectado.
class ChatDetailViewModel(
    // ID de la conversación que esta instancia observa.
    private val chatId: String,
    private val repository: ChatRepository = FirestoreChatRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    // Estado agregado de la pantalla.
    // Se usa data class (no sealed) porque varias dimensiones pueden coexistir:
    // mensajes cargados + envío en curso + error transitorio.
    data class UiState(
        // True mientras esperamos la primera emisión de mensajes.
        val isLoading: Boolean = true,
        // Lista cronológica (ascendente) de mensajes.
        val messages: List<Message> = emptyList(),
        // UID del usuario actual; la UI lo necesita para diferenciar mensajes propios.
        val currentUserId: String? = null,
        // Nombre del otro participante para mostrar en el encabezado.
        val otherParticipantName: String = "",
        // Mensaje de error transitorio para mostrar como Snackbar.
        val errorMessage: String? = null,
        // True mientras hay un envío en curso, para deshabilitar el botón de enviar.
        val isSending: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Nombre del usuario actual; se calcula una vez y se reutiliza al enviar mensajes.
    // Se obtiene de FirebaseAuth (displayName o, si no hay, la parte local del email).
    private var currentUserName: String = ""

    init {
        // Resolvemos el usuario actual y cargamos los metadatos del chat (header)
        // y luego empezamos a observar mensajes en tiempo real.
        bootstrap()
        observeMessages()
    }

    // Inicialización: usuario actual + nombre del otro participante.
    private fun bootstrap() {
        val current = auth.currentUser
        currentUserName = current?.displayName
            ?: current?.email?.substringBefore("@")
            ?: ""

        _uiState.update { it.copy(currentUserId = current?.uid) }

        viewModelScope.launch {
            runCatching { repository.getConversation(chatId) }
                .onSuccess { conversation ->
                    val otherId = conversation
                        ?.participantIds
                        ?.firstOrNull { it != current?.uid }
                    val otherName = conversation?.participantNames?.get(otherId).orEmpty()
                    _uiState.update { it.copy(otherParticipantName = otherName) }
                }
                .onFailure { error ->
                    // El header puede quedar vacío sin romper la pantalla, pero igual
                    // exponemos el error para que la UI lo muestre en un Snackbar.
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message ?: "No se pudo cargar el chat",
                        )
                    }
                }
        }
    }

    // Suscripción en tiempo real a los mensajes del chat.
    private fun observeMessages() {
        viewModelScope.launch {
            repository.observeMessages(chatId)
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al cargar mensajes",
                        )
                    }
                }
                .collect { messages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = messages,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    // Envía un mensaje al chat actual.
    // Valida que el texto no esté vacío y que exista usuario autenticado.
    fun sendMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty()) return

        val uid = auth.currentUser?.uid
        if (uid == null) {
            _uiState.update { it.copy(errorMessage = "Sesión no válida") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            repository.sendMessage(
                chatId = chatId,
                senderId = uid,
                senderName = currentUserName,
                text = text,
            ).onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "No se pudo enviar el mensaje",
                    )
                }
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    // Llamado por la UI tras mostrar el error para limpiarlo y evitar repetirlo.
    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    companion object {
        // Factory para inyectar el chatId al ViewModel cuando lo solicitemos
        // desde Compose con viewModel(factory = ChatDetailViewModel.factory(chatId)).
        fun factory(chatId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ChatDetailViewModel(chatId = chatId)
            }
        }
    }
}