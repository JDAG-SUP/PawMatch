package com.pawmatch.app.ui.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Tune
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
fun SwipeScreen(modifier: Modifier = Modifier, onNavigateToFilters: () -> Unit = {}) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()

    var currentUser by remember { mutableStateOf<User?>(null) }
    var petsToSwipe by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var isProfileIncomplete by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                val userDoc = db.collection("users").document(currentUserId).get().await()
                currentUser = userDoc.toObject(User::class.java)

                if (currentUser != null) {
                    val name = userDoc.getString("name") ?: ""
                    val petName = userDoc.getString("petName") ?: ""
                    if (name.isBlank() || petName.isBlank()) {
                        isProfileIncomplete = true
                    } else {
                        isProfileIncomplete = false
                        val animalType = userDoc.getString("preferenceAnimalType") ?: "Ambos"
                        val minAge = userDoc.getDouble("preferenceMinAge")?.toInt() ?: 0
                        val maxAge = userDoc.getDouble("preferenceMaxAge")?.toInt() ?: 20
                        val userCity = userDoc.getString("petLocation")?.takeIf { it.isNotBlank() } ?: "Ciudad de México"

                        seedDatabaseIfEmpty(db, userCity, animalType.ifEmpty { "Perro" }, currentUserId)

                        var petQuery = db.collection("pets")
                            .whereEqualTo("city", userCity)
                    
                    if (animalType != "Ambos" && animalType.isNotEmpty()) {
                        petQuery = petQuery.whereEqualTo("animalType", animalType)
                    }

                    val results = petQuery.get().await()
                    val petsList = results.documents.mapNotNull { it.toObject(Pet::class.java) }
                    
                    // Client-side filtering for Age due to Firebase inequality limitations
                    petsToSwipe = petsList.filter { pet ->
                        val petAge = pet.age.filter { it.isDigit() }.toIntOrNull() ?: 0
                        pet.ownerId != currentUserId && petAge in minAge..maxAge
                    }
                }
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
            } else if (isProfileIncomplete) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Text("¡Bienvenido a PetMatch!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Por favor completa tu Perfil y el de tu mascota desde la sección 'Perfil' (Configuración) para poder descubrir compañeros peludos.", 
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
            
            // Custom Top Bar overlay over cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PetMatch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(40.dp).clickable { onNavigateToFilters() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Filtros", tint = MaterialTheme.colorScheme.onBackground)
                        Box(modifier = Modifier.size(8.dp).offset(10.dp, (-10).dp).background(Color.Red, CircleShape)) // Red notification dot
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

        // Por petición del usuario, se hace el Match instantáneo en lugar de requerir doble opt-in mutuo
        val matchId = if (currentUserId < targetOwnerId) "${currentUserId}_${targetOwnerId}" else "${targetOwnerId}_${currentUserId}"
        val match = Match(id = matchId, userAId = currentUserId, userBId = targetOwnerId, timestamp = System.currentTimeMillis())
        db.collection("matches").document(matchId).set(match).await()
        
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
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp, bottom = 60.dp, start = 16.dp, end = 16.dp) // Adjusted padding
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
            )
            // REMOVED CLIP FROM HERE SO BUTTONS CAN ESCAPE
    ) {
        
        // VISUAL CARD CONTAINER (CLIPPED)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.LightGray)
        ) {
            if (pet.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = pet.imageUrls[0],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                 Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                     Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.3f))
                 }
            }

            // Glassmorphism Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 0f
                        )
                    )
            )

            // Progress Bar Mock (Top)
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                 Box(modifier = Modifier.weight(1f).height(3.dp).background(Color.White, RoundedCornerShape(2.dp)))
                 Box(modifier = Modifier.weight(1f).height(3.dp).background(Color.White.copy(alpha=0.3f), RoundedCornerShape(2.dp)))
            }

            // Contenido Textual
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .padding(bottom = 70.dp) // Leave space so text doesn't overlap with outside buttons
            ) {
                Text(text = "${pet.name}, ${pet.age}", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = pet.breed, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "A 1 km", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = pet.shortDescription, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primaryContainer)
            }
        } // End of Visual Card Container

        // Overlay Buttons (Completely safe from Clipping now)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .offset(y = 36.dp) // Protruding outwards
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(72.dp).clickable {
                        if(isTopCard) coroutineScope.launch {
                            launch { offsetX.animateTo(-1500f, tween(300)) }
                            launch { rotation.animateTo(-15f, tween(300)) }
                            kotlinx.coroutines.delay(200)
                            onSwipeLeft()
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         Icon(Icons.Default.Close, contentDescription = "Pass", tint = Color(0xFFFF5252), modifier = Modifier.size(36.dp))
                    }
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(72.dp).clickable {
                        if(isTopCard) coroutineScope.launch {
                            launch { offsetX.animateTo(1500f, tween(300)) }
                            launch { rotation.animateTo(15f, tween(300)) }
                            kotlinx.coroutines.delay(200)
                            onSwipeRight()
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color(0xFF4DE879), modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}
