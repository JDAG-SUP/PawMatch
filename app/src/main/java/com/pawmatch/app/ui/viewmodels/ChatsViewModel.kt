package com.pawmatch.app.ui.viewmodels

/*
 * Copyright 2026 Vincent Tsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.pawmatch.app.data.chat.ChatRepository
import com.pawmatch.app.data.chat.FirestoreChatRepository
import com.pawmatch.app.models.Conversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

// Estado de UI sellado para la pantalla de lista de chats.
// La UI solo necesita observar este tipo y renderizar según el caso.
sealed interface ChatsUiState {
    // Aún se está cargando la primera emisión desde Firestore.
    data object Loading : ChatsUiState

    // No hay usuario autenticado: la UI puede mostrar un mensaje o redirigir a login.
    data object NotAuthenticated : ChatsUiState

    // El usuario está autenticado pero no tiene conversaciones todavía.
    data class Empty(val currentUserId: String) : ChatsUiState

    // Hay conversaciones reales para mostrar.
    data class Content(
        val currentUserId: String,
        val conversations: List<Conversation>,
    ) : ChatsUiState

    // Error reportado por Firestore o por la red.
    data class Error(val message: String) : ChatsUiState
}

// ViewModel responsable de exponer el estado de la lista de conversaciones del usuario actual.
// Mantiene toda la lógica fuera de la UI tal como pide el requisito del proyecto.
class ChatsViewModel(
    // Inyectables para facilitar pruebas; los defaults usan los singletons de Firebase.
    private val repository: ChatRepository = FirestoreChatRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : ViewModel() {

    // Estado mutable interno; la UI solo ve la versión inmutable.
    private val _uiState = MutableStateFlow<ChatsUiState>(ChatsUiState.Loading)
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    init {
        // Al instanciarse, comenzamos a observar las conversaciones del usuario.
        observeConversations()
    }

    // Lanza la corrutina que escucha en tiempo real las conversaciones del usuario actual
    // y traduce cada emisión a un ChatsUiState para la UI.
    private fun observeConversations() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            // Sin usuario autenticado no podemos consultar nada.
            _uiState.value = ChatsUiState.NotAuthenticated
            return
        }

        viewModelScope.launch {
            repository.observeConversations(currentUserId)
                // Capturamos cualquier error del Flow (cierre del listener de Firestore, etc.)
                // para reflejarlo en el estado de la UI.
                .catch { error ->
                    _uiState.value = ChatsUiState.Error(
                        message = error.message ?: "No se pudieron cargar los chats",
                    )
                }
                .collect { conversations ->
                    // Si la lista llega vacía mostramos el estado vacío,
                    // de lo contrario el contenido real.
                    _uiState.value = if (conversations.isEmpty()) {
                        ChatsUiState.Empty(currentUserId)
                    } else {
                        ChatsUiState.Content(
                            currentUserId = currentUserId,
                            conversations = conversations,
                        )
                    }
                }
        }
    }
}