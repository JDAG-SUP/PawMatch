package com.pawmatch.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Perfil no encontrado \uD83D\uDE14", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Pet Cover Image Header
            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                if (pet != null && pet!!.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = pet!!.imageUrls.first(),
                        contentDescription = "Pet Cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Bottom gradient for smooth transition
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                            startY = 600f
                        )
                    ))
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer))
                }
            }

            // Profile info body overlapping cover
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-40).dp)
                    .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                if (pet != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = pet!!.name, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.padding(top=8.dp)) {
                            Text(text = pet!!.age, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal=12.dp, vertical=6.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${pet!!.breed} • ${pet!!.city}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = "Sobre mí", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = pet!!.shortDescription, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(modifier = Modifier.height(32.dp))

                // Owner Section
                Text(text = "Conoce a mi dueño", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = user!!.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (user!!.bio.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = user!!.bio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        if (user!!.hobbies.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                user!!.hobbies.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { hobby ->
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(text = hobby, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal=16.dp, vertical=8.dp), color=MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // padding for bottom button
            }
        }

        // Floating WhatsApp Button
        if (user!!.whatsappNumber.isNotEmpty()) {
            Button(
                onClick = {
                    val formattedNumber = user!!.whatsappNumber.replace("+", "").replace(" ", "")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedNumber"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp Green
            ) {
                Text("Hablar por WhatsApp", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}
