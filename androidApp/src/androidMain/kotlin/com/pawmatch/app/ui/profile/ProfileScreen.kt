package com.pawmatch.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawmatch.app.shared.presentation.viewmodels.ProfileViewModel
import com.pawmatch.app.ui.theme.PrimaryPink
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = koinInject()) {
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
            val user = uiState.profile
            val pets = uiState.myPets

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Cabecera Humano
                item {
                    Text(
                        text = "Mi Perfil",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = user?.displayName ?: "Sin Nombre", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = user?.bio ?: "Añade una bio...", color = Color.Gray)
                        }
                    }
                }

                // Mascotas
                item {
                    Text(
                        text = "Mis Mascotas Registradas",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colors.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (pets.isEmpty()) {
                    item {
                        Text("No tienes mascotas registradas.", color = Color.Gray)
                    }
                } else {
                    items(pets) { pet ->
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
                                    Text(text = pet.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "${pet.species} - ${pet.breed ?: "?"}", color = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.deletePet(pet.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
