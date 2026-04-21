package com.pawmatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    
    val chatList = listOf(
        ChatModel(name = "Carlos & Max", time = "12:30", message = "¡Hola! Qué lindo Max, ¿les gustaría ir al parq...", imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=150&q=80", isOnline = true),
        ChatModel(name = "Ana & Luna", time = "12:30", message = "¡Hola! Qué lindo Luna, ¿les gustaría ir al...", imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=150&q=80", isOnline = true),
        ChatModel(name = "Luis & Charlie", time = "12:30", message = "¡Hola! Qué lindo Charlie, ¿les gustaría ir al...", imageUrl = "https://images.unsplash.com/photo-1537151608804-ea2f1fa3dfc2?auto=format&fit=crop&w=150&q=80", isOnline = true)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Mock
        Text(
            text = "Mensajes", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.ExtraBold, 
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar chats...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Event Row
            item {
                EventChatRow()
                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.05f))
            }
            
            // DMs
            items(chatList) { chat ->
                DirectMessageRow(chat)
                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.05f))
            }
        }
    }
}

@Composable
fun EventChatRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Event Icon
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.3f), RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
            // Red dot
            Box(modifier = Modifier.size(12.dp).offset(x = 4.dp, y = (-4).dp).background(Color(0xFFE57373), CircleShape).border(2.dp, MaterialTheme.colorScheme.background, CircleShape))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Caminata Perruna", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("EVENTO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Carlos: ¡Ya estamos en la entrada principal!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Text("Ahora", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DirectMessageRow(chat: ChatModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha=0.5f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = chat.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
            if (chat.isOnline) {
                Box(modifier = Modifier.size(16.dp).background(Color(0xFF00C853), CircleShape).border(2.dp, MaterialTheme.colorScheme.background, CircleShape))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(chat.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(chat.time, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(chat.message, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

data class ChatModel(val name: String, val time: String, val message: String, val imageUrl: String, val isOnline: Boolean)
