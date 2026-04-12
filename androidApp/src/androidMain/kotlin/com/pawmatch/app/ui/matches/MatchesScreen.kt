package com.pawmatch.app.ui.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawmatch.app.shared.presentation.viewmodels.MatchViewModel
import com.pawmatch.app.ui.theme.PrimaryPink
import org.koin.compose.koinInject

@Composable
fun MatchesScreen(
    onChatClick: (String) -> Unit,
    viewModel: MatchViewModel = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PrimaryPink
            )
        } else if (uiState.error != null) {
            Text(
                "Error: ${uiState.error}",
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else {
            val matches = uiState.matches

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                item {
                    Text(
                        text = "Mis Matches 🐶",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }

                if (matches.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "Aún no tienes matches. ¡Ve al feed y desliza hacia la derecha!",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    items(matches) { match ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Match #${match.matchId.take(4).uppercase()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Con la mascota ID: ${match.matchedPetId.take(6)}...", color = Color.Gray, fontSize = 14.sp)
                                }
                                IconButton(onClick = { onChatClick(match.matchId) }, modifier = Modifier.background(PrimaryPink.copy(alpha = 0.1f), RoundedCornerShape(8.dp))) {
                                    Icon(Icons.Default.Email, contentDescription = "Chatear", tint = PrimaryPink)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
