package com.pawmatch.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.pawmatch.app.models.Pet
import com.pawmatch.app.models.User
import kotlinx.coroutines.tasks.await

@Composable
fun PublicProfileScreen(userId: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    var user by remember { mutableStateOf<User?>(null) }
    var pet by remember { mutableStateOf<Pet?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        try {
            val userDoc = db.collection("users").document(userId).get().await()
            user = userDoc.toObject(User::class.java)

            val petQuery = db.collection("pets").whereEqualTo("ownerId", userId).get().await()
            pet = petQuery.documents.firstOrNull()?.toObject(Pet::class.java)
        } catch (e: Exception) {
            // handle error
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Usuario no encontrado")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pet Info First
        if (pet != null) {
            if (pet!!.imageUrls.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    AsyncImage(
                        model = pet!!.imageUrls.first(),
                        contentDescription = "Pet Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Text(text = pet!!.name, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text(text = "${pet!!.breed} • ${pet!!.age} • ${pet!!.city}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = pet!!.shortDescription, style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))

        // Owner Info
        Text(text = "Información del Dueño", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Nombre: ${user!!.name}", style = MaterialTheme.typography.bodyLarge)
        if (user!!.bio.isNotEmpty()) {
             Text(text = "Bio: ${user!!.bio}", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Contact Button (WhatsApp)
        if (user!!.whatsappNumber.isNotEmpty()) {
            Button(
                onClick = {
                    val formattedNumber = user!!.whatsappNumber.replace("+", "").replace(" ", "")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedNumber"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Contactar al WhatsApp", style = MaterialTheme.typography.titleMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
