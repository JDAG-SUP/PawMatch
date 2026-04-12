package com.pawmatch.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawmatch.app.shared.presentation.viewmodels.ChatViewModel
import com.pawmatch.app.ui.theme.PrimaryPink
import org.koin.compose.koinInject

@Composable
fun ChatScreen(
    matchId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(matchId) {
        viewModel.initialize(matchId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat del Match ❤️", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                backgroundColor = PrimaryPink
            )
        },
        bottomBar = {
            Surface(elevation = 8.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar", tint = if (inputText.isNotBlank()) PrimaryPink else Color.Gray)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryPink
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    reverseLayout = true
                ) {
                    // Ponemos list reversed porque el newest abajo por reverseLayout
                    items(uiState.messages.reversed()) { message ->
                        ChatBubble(
                            text = message.content,
                            isFromMe = message.isFromMe,
                            time = "Ahora" // Para el MVP omitimos formateo de fechas
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isFromMe: Boolean, time: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isFromMe) 16.dp else 0.dp,
                    bottomEnd = if (isFromMe) 0.dp else 16.dp
                ),
                color = if (isFromMe) PrimaryPink else Color.White,
                elevation = 1.dp
            ) {
                Text(
                    text = text,
                    color = if (isFromMe) Color.White else Color.Black,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 16.sp
                )
            }
            Text(
                text = time,
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}
