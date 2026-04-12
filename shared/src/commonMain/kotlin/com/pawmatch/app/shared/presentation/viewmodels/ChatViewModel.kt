package com.pawmatch.app.shared.presentation.viewmodels

import com.pawmatch.app.shared.domain.entities.ChatMessage
import com.pawmatch.app.shared.domain.repositories.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatState(
    val matchId: String = "",
    val isLoading: Boolean = true,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ChatState())
    val uiState: StateFlow<ChatState> = _uiState.asStateFlow()

    fun initialize(matchId: String) {
        if (_uiState.value.matchId == matchId) return
        _uiState.update { it.copy(matchId = matchId, isLoading = true, messages = emptyList()) }
        loadHistoryAndSubscribe(matchId)
    }

    private fun loadHistoryAndSubscribe(matchId: String) {
        viewModelScope.launch {
            val historyRes = chatRepository.loadHistory(matchId)
            if (historyRes.isSuccess) {
                _uiState.update { it.copy(isLoading = false, messages = historyRes.getOrNull()!!) }
                
                // Suscribirse a mensajes nuevos
                chatRepository.observeChat(matchId).collect { newMessage ->
                    val currentList = _uiState.value.messages
                    if (currentList.none { it.id == newMessage.id }) {
                        _uiState.update { state ->
                            state.copy(messages = state.messages + newMessage)
                        }
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = historyRes.exceptionOrNull()?.message) }
            }
        }
    }

    fun sendMessage(content: String) {
        val matchId = _uiState.value.matchId
        if (content.isBlank() || matchId.isEmpty()) return
        
        viewModelScope.launch {
            // Optimistic insert podría ir aquí, pero en este MVP delegamos en la base de datos
            // para que todo mensaje validado ingrese a través del socket `observeChat` inclusive los nuestros.
            chatRepository.sendMessage(matchId, content)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
