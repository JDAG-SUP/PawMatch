/*
 * Copyright 2026 Vincent Tsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pawmatch.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pawmatch.app.models.Pet
import com.pawmatch.app.models.User
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val currentUserId = auth.currentUser?.uid ?: ""
    val currentUserEmail = auth.currentUser?.email ?: ""
    
    // User Form State
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bogotá") }
    var whatsappNumber by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var preferenceAnimalType by remember { mutableStateOf("Perro") }

    // Pet Form State
    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petAnimalType by remember { mutableStateOf("Perro") }
    var petDescription by remember { mutableStateOf("") }
    var petImage by remember { mutableStateOf("") } // MVP: URL de foto simple

    var isSaving by remember { mutableStateOf(false) }

    // Listas desplegables
    val cities = listOf("Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena")
    var expandedCity by remember { mutableStateOf(false) }

    val animalTypes = listOf("Perro", "Gato", "Ave", "Otro")
    var expandedPrefAnimal by remember { mutableStateOf(false) }
    var expandedPetAnimal by remember { mutableStateOf(false) }

    // Cargar perfil existente si lo hay
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                val userDoc = db.collection("users").document(currentUserId).get().await()
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    name = user.name
                    city = user.city.ifEmpty { "Bogotá" }
                    whatsappNumber = user.whatsappNumber
                    bio = user.bio
                    preferenceAnimalType = user.preferenceAnimalType.ifEmpty { "Perro" }
                }

                // Buscar la primer mascota (MVP)
                val petQuery = db.collection("pets").whereEqualTo("ownerId", currentUserId).get().await()
                if (!petQuery.isEmpty) {
                    val pet = petQuery.documents.first().toObject(Pet::class.java)
                    if (pet != null) {
                        petName = pet.name
                        petAnimalType = pet.animalType.ifEmpty { "Perro" }
                        petBreed = pet.breed
                        petAge = pet.age
                        petDescription = pet.shortDescription
                        if (pet.imageUrls.isNotEmpty()) petImage = pet.imageUrls.first()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tu Perfil Dueño", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = whatsappNumber, onValueChange = { whatsappNumber = it }, label = { Text("Número WhatsApp") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("Biografía") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown menu for cities
        ExposedDropdownMenuBox(expanded = expandedCity, onExpandedChange = { expandedCity = !expandedCity }) {
            OutlinedTextField(
                value = city, onValueChange = {}, readOnly = true, label = { Text("Ciudad") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCity) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedCity, onDismissRequest = { expandedCity = false }) {
                cities.forEach { selectionOption ->
                    DropdownMenuItem(text = { Text(selectionOption) }, onClick = { city = selectionOption; expandedCity = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown Pref Animal
        ExposedDropdownMenuBox(expanded = expandedPrefAnimal, onExpandedChange = { expandedPrefAnimal = !expandedPrefAnimal }) {
            OutlinedTextField(
                value = preferenceAnimalType, onValueChange = {}, readOnly = true, label = { Text("Busco conectar con (Animal)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPrefAnimal) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedPrefAnimal, onDismissRequest = { expandedPrefAnimal = false }) {
                animalTypes.forEach { selectionOption ->
                    DropdownMenuItem(text = { Text(selectionOption) }, onClick = { preferenceAnimalType = selectionOption; expandedPrefAnimal = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        Text("Perfil de tu Mascota", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = petName, onValueChange = { petName = it }, label = { Text("Nombre Mascota") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown Pet Animal
        ExposedDropdownMenuBox(expanded = expandedPetAnimal, onExpandedChange = { expandedPetAnimal = !expandedPetAnimal }) {
            OutlinedTextField(
                value = petAnimalType, onValueChange = {}, readOnly = true, label = { Text("Especie de tu Mascota") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPetAnimal) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedPetAnimal, onDismissRequest = { expandedPetAnimal = false }) {
                animalTypes.forEach { selectionOption ->
                    DropdownMenuItem(text = { Text(selectionOption) }, onClick = { petAnimalType = selectionOption; expandedPetAnimal = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = petBreed, onValueChange = { petBreed = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = petAge, onValueChange = { petAge = it }, label = { Text("Edad (Ej. 2 años)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = petDescription, onValueChange = { petDescription = it }, label = { Text("Descripción Corta") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = petImage, onValueChange = { petImage = it }, label = { Text("URL de Imagen") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    isSaving = true
                    try {
                        // Save User with Timeout to avoid infinite hangs if Firestore is uninitialized
                        kotlinx.coroutines.withTimeout(8000L) {
                            val user = User(
                                id = currentUserId, name = name, email = currentUserEmail,
                                city = city, whatsappNumber = whatsappNumber, bio = bio,
                                preferenceAnimalType = preferenceAnimalType
                            )
                            db.collection("users").document(currentUserId).set(user).await()

                            val petId = "${currentUserId}_pet"
                            val pet = Pet(
                                id = petId, ownerId = currentUserId, name = petName,
                                animalType = petAnimalType, breed = petBreed, age = petAge,
                                city = city, shortDescription = petDescription, 
                                imageUrls = if (petImage.isNotEmpty()) listOf(petImage) else emptyList()
                            )
                            db.collection("pets").document(petId).set(pet).await()
                        }
                        Toast.makeText(context, "Perfil guardado con éxito", Toast.LENGTH_SHORT).show()
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        Toast.makeText(context, "Error: Revisa que hayas creado Cloud Firestore en tu consola.", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isSaving = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Guardar Todo")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = {
                auth.signOut()
                // Require app reboot or state handling to show auth screen again
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
