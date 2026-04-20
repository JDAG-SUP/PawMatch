package com.pawmatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchingPreferencesScreen() {
    var selectedPetType by remember { mutableStateOf("Ambos") }
    var locationSwitch by remember { mutableStateOf(true) }
    var maxDistance by remember { mutableStateOf(15f) }
    var minAge by remember { mutableStateOf(0f) }
    var maxAge by remember { mutableStateOf(5f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // App Bar Mock
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(32.dp))
            Text("Preferencias de Matching", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.1f))

        Column(modifier = Modifier.padding(24.dp)) {
            // Pet Type Chips
            Text("Tipo de mascota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PetTypeChip("Perros", selectedPetType == "Perros") { selectedPetType = "Perros" }
                PetTypeChip("Gatos", selectedPetType == "Gatos") { selectedPetType = "Gatos" }
                PetTypeChip("Ambos", selectedPetType == "Ambos") { selectedPetType = "Ambos" }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Breeds
            Text("Razas de interés (Opcional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BreedTag("Golden Retriever")
                BreedTag("Pug")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Añadir razas...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Location
            Text("Ubicación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mi ubicación actual", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = locationSwitch,
                            onCheckedChange = { locationSwitch = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.surface, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                    Divider(color = Color(0xFFE0E0E0))
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color(0xFFF4F4F4), RoundedCornerShape(8.dp)).padding(16.dp)) {
                        Text("Ciudad de México", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Distance
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Distancia máxima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${maxDistance.toInt()} km", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = maxDistance,
                onValueChange = { maxDistance = it },
                valueRange = 1f..100f,
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Age
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Edad de la mascota", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${minAge.toInt()} - ${maxAge.toInt()} años", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            RangeSlider(
                value = minAge..maxAge,
                onValueChange = { minAge = it.start; maxAge = it.endInclusive },
                valueRange = 0f..20f,
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Aplicar Filtros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Restablecer filtros", style = MaterialTheme.typography.bodyLarge, color = Color.Gray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RowScope.PetTypeChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BreedTag(text: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
    }
}
