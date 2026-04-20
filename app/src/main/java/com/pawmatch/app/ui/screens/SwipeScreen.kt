package com.pawmatch.app.ui.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.pawmatch.app.utils.seedDatabaseIfEmpty
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
                val userDoc = db.collection("users").document(currentUserId).get().await()
                currentUser = userDoc.toObject(User::class.java)

                if (currentUser != null && currentUser!!.city.isNotEmpty()) {
                    
                    // Llama al script de semillas de forma pasiva para UI
                    seedDatabaseIfEmpty(db, currentUser!!.city, currentUser!!.preferenceAnimalType.ifEmpty { "Perro" })

                    val petQuery = db.collection("pets")
                        .whereEqualTo("city", currentUser!!.city)
                        .whereEqualTo("animalType", currentUser!!.preferenceAnimalType)
                        .get().await()
                        
                    val petsList = petQuery.documents.mapNotNull { it.toObject(Pet::class.java) }
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
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (currentUser?.city.isNullOrEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("¡Bienvenido a PawMatch!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Por favor completa tu Perfil Primero desde la barra de navegación para poder descubrir compañeros peludos.", 
                        style = MaterialTheme.typography.bodyLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else if (petsToSwipe.isEmpty()) {
                Text("No hay más mascotas en tu zona \uD83D\uDE22", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
            } else {
                petsToSwipe.forEachIndexed { index, pet ->
                    if (index >= petsToSwipe.size - 2) {
                        key(pet.id) {
                            SwipeCardWithGestures(
                                pet = pet,
                                isTopCard = index == petsToSwipe.size - 1,
                                onSwipeLeft = {
                                    petsToSwipe = petsToSwipe.filter { it.id != pet.id }
                                },
                                onSwipeRight = {
                                    coroutineScope.launch {
                                        handleLikeAction(currentUserId, pet.ownerId, db)
                                        petsToSwipe = petsToSwipe.filter { it.id != pet.id }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

suspend fun handleLikeAction(currentUserId: String, targetOwnerId: String, db: FirebaseFirestore) {
    try {
        db.collection("likes").document("${currentUserId}_${targetOwnerId}")
            .set(mapOf("from" to currentUserId, "to" to targetOwnerId, "timestamp" to System.currentTimeMillis()))
            .await()

        val reverseLike = db.collection("likes").document("${targetOwnerId}_${currentUserId}").get().await()
        if (reverseLike.exists()) {
            val matchId = if (currentUserId < targetOwnerId) "${currentUserId}_${targetOwnerId}" else "${targetOwnerId}_${currentUserId}"
            val match = Match(id = matchId, userAId = currentUserId, userBId = targetOwnerId, timestamp = System.currentTimeMillis())
            db.collection("matches").document(matchId).set(match).await()
        }
    } catch (e: Exception) {
        Log.e("SwipeScreen", "Error liking", e)
    }
}

@Composable
fun SwipeCardWithGestures(
    pet: Pet,
    isTopCard: Boolean,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var currentImageIndex by remember { mutableIntStateOf(0) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.85f)
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .graphicsLayer {
                rotationZ = rotation.value
                scaleX = if (!isTopCard) 0.95f else 1f
                scaleY = if (!isTopCard) 0.95f else 1f
            }
            .then(
                if (isTopCard) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                coroutineScope.launch {
                                    if (offsetX.value > 300f) {
                                        launch { offsetX.animateTo(1500f, tween(300)) }
                                        launch { rotation.animateTo(15f, tween(300)) }
                                        kotlinx.coroutines.delay(200)
                                        onSwipeRight()
                                    } else if (offsetX.value < -300f) {
                                        launch { offsetX.animateTo(-1500f, tween(300)) }
                                        launch { rotation.animateTo(-15f, tween(300)) }
                                        kotlinx.coroutines.delay(200)
                                        onSwipeLeft()
                                    } else {
                                        launch { offsetX.animateTo(0f, tween(300)) }
                                        launch { offsetY.animateTo(0f, tween(300)) }
                                        launch { rotation.animateTo(0f, tween(300)) }
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    offsetY.snapTo(offsetY.value + dragAmount.y * 0.3f)
                                    rotation.snapTo(offsetX.value / 40f)
                                }
                            }
                        )
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTopCard) 12.dp else 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (pet.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = pet.imageUrls[currentImageIndex],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                 Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                     Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.3f))
                 }
            }

            // Glassmorphism Overlay Gradiente
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 0f
                        )
                    )
            )

            // Contenido Textual Premium
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = pet.name, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = pet.age, style = MaterialTheme.typography.headlineSmall, color = Color.White.copy(alpha = 0.9f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${pet.breed} • ${pet.city}", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.85f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = pet.shortDescription, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones Action
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(
                        onClick = { 
                            if(isTopCard) coroutineScope.launch {
                                launch { offsetX.animateTo(-1500f, tween(300)) }
                                launch { rotation.animateTo(-15f, tween(300)) }
                                kotlinx.coroutines.delay(200)
                                onSwipeLeft()
                            }
                        },
                        modifier = Modifier.size(72.dp).shadow(12.dp, CircleShape).clip(CircleShape).background(Color.White)
                    ) { Icon(Icons.Default.Close, contentDescription = "Pass", tint = Color(0xFFFF5252), modifier = Modifier.size(40.dp)) }
                    
                    IconButton(
                        onClick = { 
                             if(isTopCard) coroutineScope.launch {
                                launch { offsetX.animateTo(1500f, tween(300)) }
                                launch { rotation.animateTo(15f, tween(300)) }
                                kotlinx.coroutines.delay(200)
                                onSwipeRight()
                            }
                        },
                        modifier = Modifier.size(72.dp).shadow(12.dp, CircleShape).clip(CircleShape).background(Color.White)
                    ) { Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color(0xFF4CAF50), modifier = Modifier.size(40.dp)) }
                }
            }
        }
    }
}
