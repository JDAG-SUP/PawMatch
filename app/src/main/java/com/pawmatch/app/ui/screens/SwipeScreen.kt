package com.pawmatch.app.ui.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pawmatch.app.models.Pet
import com.pawmatch.app.models.User
import com.pawmatch.app.models.Match
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun SwipeScreen(modifier: Modifier = Modifier) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()

    var currentUser by remember { mutableStateOf<User?>(null) }
    var petsToSwipe by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                // Traer datos del usuario activo para saber Filtros (Ciudad, Preferencia).
                val userDoc = db.collection("users").document(currentUserId).get().await()
                currentUser = userDoc.toObject(User::class.java)

                if (currentUser != null && currentUser!!.city.isNotEmpty()) {
                    // Consultar mascotas (Misma Ciudad, Que se adapten a la preferencia del usuario)
                    val petQuery = db.collection("pets")
                        .whereEqualTo("city", currentUser!!.city)
                        .whereEqualTo("animalType", currentUser!!.preferenceAnimalType)
                        .get().await()
                        
                    val petsList = petQuery.documents.mapNotNull { it.toObject(Pet::class.java) }
                    
                    // Filtrar la tuya propia (No puedes hacerte match a ti mismo)
                    // Además, idealmente filtraríamos las que ya dimos like/pass (Omitido para MVP si es muy complejo)
                    petsToSwipe = petsList.filter { it.ownerId != currentUserId }
                }
            } catch (e: Exception) {
                Log.e("SwipeScreen", "Error loading data", e)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (currentUser?.city.isNullOrEmpty()) {
                Text("Por favor completa tu Perfil primero.")
            } else if (petsToSwipe.isEmpty()) {
                Text("No hay más mascotas en tu zona.")
            } else {
                // Mostramos el último en la lista. Pop de la Pila.
                val topPet = petsToSwipe.last()
                
                SwipeCardWithGestures(
                    pet = topPet,
                    onSwipeLeft = {
                        petsToSwipe = petsToSwipe.dropLast(1)
                    },
                    onSwipeRight = {
                        // Like!
                        coroutineScope.launch {
                            handleLikeAction(currentUserId, topPet.ownerId, db)
                            petsToSwipe = petsToSwipe.dropLast(1)
                        }
                    }
                )
            }
        }
    }
}

// Función mock para validar Match
suspend fun handleLikeAction(currentUserId: String, targetOwnerId: String, db: FirebaseFirestore) {
    try {
        // Registrar Like (A -> B)
        db.collection("likes").document("${currentUserId}_${targetOwnerId}")
            .set(mapOf("from" to currentUserId, "to" to targetOwnerId, "timestamp" to System.currentTimeMillis()))
            .await()

        // Revisar si B le dio Like a A
        val reverseLike = db.collection("likes").document("${targetOwnerId}_${currentUserId}").get().await()
        if (reverseLike.exists()) {
            // ¡Es un Match! Guardar en Colección de Matches
            val matchId = if (currentUserId < targetOwnerId) "${currentUserId}_${targetOwnerId}" else "${targetOwnerId}_${currentUserId}"
            val match = Match(id = matchId, userAId = currentUserId, userBId = targetOwnerId, timestamp = System.currentTimeMillis())
            db.collection("matches").document(matchId).set(match).await()
            // Podríamos disparar un estado global de "Match Encontrado" para animación.
        }
    } catch (e: Exception) {
        Log.e("SwipeScreen", "Error liking", e)
    }
}

@Composable
fun SwipeCardWithGestures(
    pet: Pet,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var currentImageIndex by remember { mutableIntStateOf(0) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .fillMaxHeight(0.7f)
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            val targetX = offsetX.value
                            if (targetX > 200f) {
                                // Swiped right
                                offsetX.animateTo(1000f, tween(300))
                                onSwipeRight()
                            } else if (targetX < -200f) {
                                // Swiped left
                                offsetX.animateTo(-1000f, tween(300))
                                onSwipeLeft()
                            } else {
                                // Vuelve al centro
                                offsetX.animateTo(0f, tween(300))
                                offsetY.animateTo(0f, tween(300))
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y * 0.5f)
                        }
                    }
                )
            }
            .graphicsLayer {
                rotationZ = offsetX.value / 20f
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (pet.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = pet.imageUrls[currentImageIndex],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (pet.imageUrls.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (currentImageIndex > 0) currentImageIndex-- }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                        IconButton(onClick = { if (currentImageIndex < pet.imageUrls.size - 1) currentImageIndex++ }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                }
            } else {
                 Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                     Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.5f))
                 }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Text(text = "${pet.name}, ${pet.age}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${pet.breed} • ${pet.city}", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = pet.shortDescription, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FloatingActionButton(
                        onClick = { 
                            coroutineScope.launch {
                                offsetX.animateTo(-1000f, tween(300))
                                onSwipeLeft()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.error,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) { Icon(Icons.Default.Close, contentDescription = "Pass", modifier = Modifier.size(32.dp)) }
                    
                    FloatingActionButton(
                        onClick = { 
                             coroutineScope.launch {
                                offsetX.animateTo(1000f, tween(300))
                                onSwipeRight()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) { Icon(Icons.Default.Favorite, contentDescription = "Like", modifier = Modifier.size(32.dp)) }
                }
            }
        }
    }
}
