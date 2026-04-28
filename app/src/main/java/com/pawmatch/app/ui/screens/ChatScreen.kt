
package com.pawmatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pawmatch.app.models.Conversation
import com.pawmatch.app.ui.viewmodels.ChatsUiState
import com.pawmatch.app.ui.viewmodels.ChatsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    // Callback opcional para abrir el detalle de un chat.
    // Se mantiene con default vacío para que MainScreen siga compilando hasta el Paso 7,
    // donde se conectará la navegación a ChatDetailScreen.
    onConversationClick: (String) -> Unit = {},
    // Se obtiene el ViewModel a través del DI por defecto de Compose.
    viewModel: ChatsViewModel = viewModel(),
) {
    // Observamos el estado del ViewModel respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Texto de búsqueda local (filtra solo en cliente, sin tocar Firestore).
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Encabezado de la pantalla.
        Text(
            text = "Mensajes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )

        // Barra de búsqueda local sobre la lista de conversaciones.
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar chats...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Renderizado en función del estado expuesto por el ViewModel.
        when (val state = uiState) {
            ChatsUiState.Loading -> CenteredLoading()
            ChatsUiState.NotAuthenticated -> CenteredMessage(
                text = "Inicia sesión para ver tus chats",
            )
            is ChatsUiState.Empty -> CenteredMessage(
                text = "Aún no tienes conversaciones",
            )
            is ChatsUiState.Error -> CenteredMessage(
                text = "Ocurrió un error: ${state.message}",
            )
            is ChatsUiState.Content -> {
                // Filtramos la lista por el texto de búsqueda local.
                // Se recalcula solo cuando cambian las dependencias.
                val filtered = remember(
                    state.conversations,
                    state.currentUserId,
                    searchQuery,
                ) {
                    val query = searchQuery.trim().lowercase(Locale.getDefault())
                    if (query.isEmpty()) {
                        state.conversations
                    } else {
                        state.conversations.filter { conversation ->
                            val name = conversation
                                .otherParticipantName(state.currentUserId)
                                .lowercase(Locale.getDefault())
                            val last = conversation.lastMessage
                                .lowercase(Locale.getDefault())
                            name.contains(query) || last.contains(query)
                        }
                    }
                }

                if (filtered.isEmpty()) {
                    // El usuario tiene chats pero el filtro actual no devuelve nada.
                    CenteredMessage(text = "Sin coincidencias para \"$searchQuery\"")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered, key = { it.id }) { conversation ->
                            ConversationRow(
                                conversation = conversation,
                                currentUserId = state.currentUserId,
                                onClick = { onConversationClick(conversation.id) },
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onBackground
                                    .copy(alpha = 0.05f),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fila individual de la lista de conversaciones.
// Muestra inicial del otro usuario, nombre, hora y último mensaje.
@Composable
private fun ConversationRow(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit,
) {
    val otherName = conversation.otherParticipantName(currentUserId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar circular con la inicial del otro participante.
        // Se usa un placeholder porque Conversation no almacena URL de foto.
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            val initial = otherName.firstOrNull()?.uppercaseChar()
            if (initial != null) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = otherName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = formatChatTime(conversation.lastMessageAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                // Si todavía no se envió ningún mensaje en el chat, mostramos un placeholder.
                text = conversation.lastMessage.ifBlank { "Comienza la conversación" },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// Indicador de carga centrado en el espacio restante.
@Composable
private fun CenteredLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

// Mensaje informativo centrado (estado vacío, error o no autenticado).
@Composable
private fun CenteredMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

// Devuelve el nombre del otro participante en una conversación 1-a-1.
// Si no se encuentra, retorna un fallback amigable.
private fun Conversation.otherParticipantName(currentUserId: String): String {
    val otherId = participantIds.firstOrNull { it != currentUserId }
        ?: return "Conversación"
    return participantNames[otherId] ?: "Usuario"
}

// Formato simple para la lista de chats:
// hoy -> "HH:mm", otro día -> "dd/MM".
// El helper definitivo se introduce en un commit posterior.
private fun formatChatTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return ""
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = epochMillis }
    val sameDay = now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)
    val pattern = if (sameDay) "HH:mm" else "dd/MM"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(epochMillis))
}