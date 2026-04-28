/*
 * Copyright 2026 Vincent Tsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.pawmatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.models.Message
import com.pawmatch.app.ui.viewmodels.ChatDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    // ID del chat a abrir; lo pasa el destino de navegación.
    chatId: String,
    // Callback para volver atrás (popBackStack).
    onNavigateBack: () -> Unit,
    // ViewModel parametrizado con el chatId mediante factory.
    viewModel: ChatDetailViewModel = viewModel(
        factory = ChatDetailViewModel.factory(chatId),
    ),
) {
    // Observamos el estado del ViewModel respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Texto en el campo de entrada; vive solo en la UI porque es transitorio.
    var draftText by remember { mutableStateOf("") }

    // Estado del LazyColumn para poder hacer auto-scroll al último mensaje.
    val listState = rememberLazyListState()

    // Host de Snackbar para mostrar errores transitorios.
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-scroll cuando llega un mensaje nuevo (cambia el tamaño de la lista).
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    // Mostramos el error en Snackbar y avisamos al ViewModel para que lo limpie.
    LaunchedEffect(uiState.errorMessage) {
        val current = uiState.errorMessage
        if (current != null) {
            snackbarHostState.showSnackbar(current)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        // Si todavía no se cargó el header mostramos un fallback.
                        text = uiState.otherParticipantName.ifBlank { "Conversación" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MessageInputBar(
                text = draftText,
                onTextChange = { draftText = it },
                isSending = uiState.isSending,
                onSend = {
                    // El ViewModel valida texto vacío internamente.
                    viewModel.sendMessage(draftText)
                    // Limpiamos el campo de inmediato; si el envío falla, el error
                    // se mostrará en Snackbar pero el usuario ya puede reintentar.
                    draftText = ""
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                // Estado de carga inicial: aún no llegó la primera emisión de mensajes.
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                // Estado vacío: el chat existe pero aún no tiene mensajes.
                uiState.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Envía el primer mensaje para iniciar la conversación",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        )
                    }
                }
                // Estado con contenido: pintamos las burbujas en orden cronológico.
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                isMine = message.senderId == uiState.currentUserId,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Burbuja individual de mensaje. Se alinea a la derecha si es del usuario actual
// y a la izquierda si es del otro participante. Cambia colores en consecuencia.
@Composable
private fun MessageBubble(
    message: Message,
    isMine: Boolean,
) {
    val bubbleColor = if (isMine) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isMine) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    // Forma asimétrica: la esquina opuesta al remitente queda más recta para
    // sugerir visualmente quién emite el mensaje.
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMine) 16.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 16.dp,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        // Alineación horizontal según el remitente.
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            // La burbuja no debe ocupar todo el ancho para distinguirse del fondo.
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .background(color = bubbleColor, shape = bubbleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatMessageTime(message.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f),
            )
        }
    }
}

// Barra inferior con campo de texto y botón de envío.
// Se mantiene como composable propio para poder reutilizarlo o testearlo.
@Composable
private fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isSending: Boolean,
    onSend: () -> Unit,
) {
    // El botón solo se habilita cuando hay texto válido y no hay otro envío en curso.
    val canSend = text.isNotBlank() && !isSending

    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Escribe un mensaje") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Contenedor circular para el botón de enviar; se atenúa cuando está deshabilitado.
            FilledIconButton(
                onClick = onSend,
                enabled = canSend,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                ),
            ) {
                if (isSending) {
                    // Indicador pequeño mientras se envía el mensaje.
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                    )
                }
            }
        }
    }
}

// Formato corto para la hora dentro de una burbuja (HH:mm en el locale del dispositivo).
// El helper definitivo se introduce en un commit posterior y reemplaza este inline.
private fun formatMessageTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))
}