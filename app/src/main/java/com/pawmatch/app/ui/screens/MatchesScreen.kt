package com.pawmatch.app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pawmatch.app.data.chat.ChatRepository
import com.pawmatch.app.data.chat.FirestoreChatRepository
import com.pawmatch.app.models.Match
import com.pawmatch.app.models.Pet
import com.pawmatch.app.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MatchDisplayData(
    val matchedUserId: String,
    val matchedUser: User,
    val matchedPet: Pet?,
)

@Composable
fun MatchesScreen(
    onNavigateToPublicProfile: (String) -> Unit,
    // Callback que recibe el chatId resuelto y navega al detalle.
    // Quien llama (MainScreen) solo se ocupa de la navegación.
    onOpenChat: (chatId: String) -> Unit,
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return

    // Repositorio de chat reutilizado entre interacciones de la pantalla.
    // Se memoriza para no instanciarlo en cada recomposición.
    val chatRepository: ChatRepository = remember { FirestoreChatRepository() }
    val coroutineScope = rememberCoroutineScope()

    var matchesList by remember { mutableStateOf<List<MatchDisplayData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Bandera para evitar disparar varias veces la apertura del chat
    // si el usuario toca el ícono repetidamente mientras se resuelve.
    var isOpeningChat by remember { mutableStateOf(false) }

    LaunchedEffect(currentUserId) {
        try {
            val queryA = db.collection("matches").whereEqualTo("userAId", currentUserId).get().await()
            val queryB = db.collection("matches").whereEqualTo("userBId", currentUserId).get().await()

            val allMatches = (queryA.documents + queryB.documents)
                .mapNotNull { it.toObject(Match::class.java) }
                .distinctBy { it.id }
                .sortedByDescending { it.timestamp } // Ordenar por más recientes

            val displayData = mutableListOf<MatchDisplayData>()

            for (match in allMatches) {
                val targetUserId = if (match.userAId == currentUserId) match.userBId else match.userAId
                val targetUserDoc = db.collection("users").document(targetUserId).get().await()
                val targetUser = targetUserDoc.toObject(User::class.java)

                if (targetUser != null) {
                    val targetPetQuery = db.collection("pets").whereEqualTo("ownerId", targetUserId).get().await()
                    val targetPet = targetPetQuery.documents.firstOrNull()?.toObject(Pet::class.java)

                    displayData.add(MatchDisplayData(targetUserId, targetUser, targetPet))
                }
            }
            matchesList = displayData
        } catch (e: Exception) {
            Log.e("MatchesScreen", "Error loading matches", e)
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp)) {
            Text("Tus Matches", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (matchesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Aún no tienes matches. Sigue deslizando para encontrar amigos para tu mascota.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                items(matchesList) { matchData ->
                    MatchItemRowPremium(
                        matchData = matchData,
                        onClick = { onNavigateToPublicProfile(matchData.matchedUserId) },
                        // Al tocar el ícono de chat resolvemos el chatId vía repositorio
                        // y delegamos la navegación al callback externo.
                        onOpenChat = {
                            if (isOpeningChat) return@MatchItemRowPremium
                            isOpeningChat = true
                            coroutineScope.launch {
                                val current = auth.currentUser
                                if (current != null) {
                                    // El nombre del usuario actual se construye desde Firebase Auth.
                                    val currentName = current.displayName
                                        ?: current.email?.substringBefore("@")
                                        ?: ""
                                    chatRepository.getOrCreateConversation(
                                        currentUserId = current.uid,
                                        currentUserName = currentName,
                                        otherUserId = matchData.matchedUserId,
                                        otherUserName = matchData.matchedUser.name,
                                    ).onSuccess { chatId ->
                                        onOpenChat(chatId)
                                    }.onFailure { error ->
                                        Log.e("MatchesScreen", "Error opening chat", error)
                                    }
                                }
                                isOpeningChat = false
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun MatchItemRowPremium(
    matchData: MatchDisplayData,
    onClick: () -> Unit,
    onOpenChat: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar de la mascota (sin cambios).
            if (matchData.matchedPet != null && matchData.matchedPet.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = matchData.matchedPet.imageUrls.first(),
                    contentDescription = "Pet Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
                )
            } else {
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("\uD83D\uDC3E", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = matchData.matchedPet?.name ?: "Mascota", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Dueño: ${matchData.matchedUser.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${matchData.matchedPet?.breed ?: "Raza"} • ${matchData.matchedPet?.city ?: ""}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            // Botón de acceso directo al chat con este match.
            // Compose absorbe el click para que NO se propague al Card.
            IconButton(onClick = onOpenChat) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Abrir chat",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}