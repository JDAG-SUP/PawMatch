package com.pawmatch.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val storage = FirebaseStorage.getInstance()
    val currentUser = auth.currentUser
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // User fields
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("Mexicana") }
    var civilStatus by remember { mutableStateOf("Soltero/a") }
    var bio by remember { mutableStateOf("Me encanta la naturaleza y pasar tiempo con mi mascota.") }
    var hobbyInput by remember { mutableStateOf("") }
    var hobbies by remember { mutableStateOf(listOf("Senderismo", "Fotografía")) }

    // User Images
    val userImages = remember { mutableStateListOf<Uri>() }
    val userPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(2)
    ) { uris ->
        userImages.clear()
        userImages.addAll(uris)
    }

    // Pet fields
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Perro") }
    var petAge by remember { mutableStateOf("4 años") }
    var petBreed by remember { mutableStateOf("Mestizo") }
    var petLocation by remember { mutableStateOf("Ciudad de México") }
    var petDesc by remember { mutableStateOf("") }

    // Pet Images
    val petImages = remember { mutableStateListOf<Uri>() }
    val petPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(8)
    ) { uris ->
        petImages.clear()
        petImages.addAll(uris)
    }

    // Fetch initial Profile data
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    name = doc.getString("name") ?: ""
                    age = doc.getString("age") ?: ""
                    nationality = doc.getString("nationality") ?: "Mexicana"
                    civilStatus = doc.getString("civilStatus") ?: "Soltero/a"
                    bio = doc.getString("bio") ?: ""
                    
                    val fetchedHobbies = doc.get("hobbies") as? List<String>
                    if (fetchedHobbies != null) {
                        hobbies = fetchedHobbies
                    }

                    petName = doc.getString("petName") ?: ""
                    petType = doc.getString("petType") ?: "Perro"
                    petAge = doc.getString("petAge") ?: "4 años"
                    petBreed = doc.getString("petBreed") ?: "Mestizo"
                    petLocation = doc.getString("petLocation") ?: "Ciudad de México"
                    petDesc = doc.getString("petDesc") ?: ""

                    val fetchedUserImages = doc.get("userImages") as? List<String>
                    if (fetchedUserImages != null) {
                        userImages.addAll(fetchedUserImages.map { Uri.parse(it) })
                    }

                    val fetchedPetImages = doc.get("petImages") as? List<String>
                    if (fetchedPetImages != null) {
                        petImages.addAll(fetchedPetImages.map { Uri.parse(it) })
                    }
                }
            } catch (e: Exception) {
                // Ignore load errors for now
            }
        }
        isLoading = false
    }

    // Header & Tabs
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.width(32.dp))
            Text("Editar Perfil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Mi Perfil", color = if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary else Color.Gray) })
            Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Mi Mascota", color = if (selectedTabIndex == 1) MaterialTheme.colorScheme.primary else Color.Gray) })
        }

        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.1f))

        // Content
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                        .padding(bottom = 80.dp) // space for button
                ) {
                    if (selectedTabIndex == 0) {
                        
                        // User Image Gallery (Max 2)
                        Row(
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            userImages.forEach { uri ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Foto de usuario",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(24.dp)
                                            .background(Color(0xE6000000), CircleShape)
                                            .clickable { userImages.remove(uri) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            // Add Photo Button (only if < 2)
                            if (userImages.size < 2) {
                                Box(
                                    modifier = Modifier
                                        .weight(if (userImages.size == 0) 1f else (1f / (2 - userImages.size)))
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                                        .clickable {
                                            userPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Añadir foto", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        Text("Nombre", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Edad", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                                OutlinedTextField(value = age, onValueChange = { age = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Nacionalidad", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                                OutlinedTextField(value = nationality, onValueChange = { nationality = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))
                            }
                        }

                        Text("Estado Civil", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(value = civilStatus, onValueChange = { civilStatus = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))

                        Text("Descripción personal", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(value = bio, onValueChange = { bio = it }, modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))

                        Text("Hobbies", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            hobbies.forEach { hobby ->
                                Row(
                                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(16.dp)).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha=0.3f), RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(hobby, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp).clickable { hobbies = hobbies.filter { it != hobby } })
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = hobbyInput,
                                onValueChange = { hobbyInput = it },
                                placeholder = { Text("Agregar hobby...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)).clickable { 
                                    if (hobbyInput.isNotBlank()) { hobbies = hobbies + hobbyInput; hobbyInput = "" } 
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            }
                        }

                    } else {
                        
                        // Galería de fotos (Mascota - Max 8)
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(petImages) { uri ->
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Foto de mascota",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(24.dp)
                                            .background(Color(0xE6000000), CircleShape)
                                            .clickable { petImages.remove(uri) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            if (petImages.size < 8) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .width(140.dp)
                                            .fillMaxHeight()
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                                            .clickable {
                                                petPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Añadir foto", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Nombre de la mascota", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(value = petName, onValueChange = { petName = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tipo de animal", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                                OutlinedTextField(value = petType, onValueChange = { petType = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Edad", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                                OutlinedTextField(value = petAge, onValueChange = { petAge = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))
                            }
                        }

                        Text("Raza", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(value = petBreed, onValueChange = { petBreed = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))

                        Text("Ubicación", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(value = petLocation, onValueChange = { petLocation = it }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))

                        Text("Descripción", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        OutlinedTextField(value = petDesc, onValueChange = { petDesc = it }, modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 4.dp, bottom = 16.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color(0xFFE0E0E0), focusedBorderColor = MaterialTheme.colorScheme.primary))
                    }
                }

                // Fixed Button at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { 
                            if (petImages.size < 4) {
                                Toast.makeText(context, "Debes subir al menos 4 fotos de tu mascota.", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            
                            val uid = currentUser?.uid ?: return@Button
                            isSaving = true
                            
                            scope.launch {
                                try {
                                    val finalUserImages = mutableListOf<String>()
                                    for (uri in userImages) {
                                        if (uri.scheme == "content" || uri.scheme == "file") {
                                            val ref = storage.reference.child("profile_images/${uid}/user_${UUID.randomUUID()}.jpg")
                                            ref.putFile(uri).await()
                                            finalUserImages.add(ref.downloadUrl.await().toString())
                                        } else {
                                            finalUserImages.add(uri.toString())
                                        }
                                    }

                                    val finalPetImages = mutableListOf<String>()
                                    for (uri in petImages) {
                                        if (uri.scheme == "content" || uri.scheme == "file") {
                                            val ref = storage.reference.child("profile_images/${uid}/pet_${UUID.randomUUID()}.jpg")
                                            ref.putFile(uri).await()
                                            finalPetImages.add(ref.downloadUrl.await().toString())
                                        } else {
                                            finalPetImages.add(uri.toString())
                                        }
                                    }

                                    val userProfile = hashMapOf(
                                        "name" to name,
                                        "age" to age,
                                        "nationality" to nationality,
                                        "civilStatus" to civilStatus,
                                        "bio" to bio,
                                        "hobbies" to hobbies,
                                        "userImages" to finalUserImages,
                                        "petName" to petName,
                                        "petType" to petType,
                                        "petAge" to petAge,
                                        "petBreed" to petBreed,
                                        "petLocation" to petLocation,
                                        "petDesc" to petDesc,
                                        "petImages" to finalPetImages
                                    )

                                    db.collection("users").document(uid).set(userProfile).await()
                                    Toast.makeText(context, "Datos guardados en la nube exitosamente", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Text("Guardar Cambios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
