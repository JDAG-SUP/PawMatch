package com.pawmatch.app.ui.discovery

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawmatch.app.shared.domain.entities.Pet
import com.pawmatch.app.shared.presentation.viewmodels.DiscoveryState
import com.pawmatch.app.shared.presentation.viewmodels.DiscoveryViewModel
import com.pawmatch.app.ui.theme.PrimaryPink
import com.pawmatch.app.ui.theme.SecondaryBlue
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DiscoveryScreen(viewModel: DiscoveryViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is DiscoveryState.Loading -> {
                CircularProgressIndicator(color = PrimaryPink)
            }
            is DiscoveryState.Error -> {
                Text(text = "Error: ${state.message}", color = Color.Red)
            }
            is DiscoveryState.Feed -> {
                if (state.pets.isEmpty()) {
                    Text(
                        text = "¡No hay más mascotas cerca de ti!",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    // Renderizamos la pila en reversa para que el primero sea el top z-index
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        state.pets.reversed().forEachIndexed { index, pet ->
                            val isTopCard = index == state.pets.size - 1
                            SwipeablePetCard(
                                pet = pet,
                                isTopCard = isTopCard,
                                onSwipeLeft = { viewModel.swipePet(pet.id, liked = false) },
                                onSwipeRight = { viewModel.swipePet(pet.id, liked = true) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeablePetCard(
    pet: Pet,
    isTopCard: Boolean,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    val swipeThreshold = 300f

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = rotation.value
                // Sutil escala si no es la principal
                scaleX = if (isTopCard) 1f else 0.95f
                scaleY = if (isTopCard) 1f else 0.95f
            }
            .pointerInput(isTopCard) {
                if (!isTopCard) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (abs(offsetX.value) > swipeThreshold) {
                                // Deslizó suficiente
                                val swipeRight = offsetX.value > 0
                                offsetX.animateTo(
                                    targetValue = if (swipeRight) 1000f else -1000f,
                                    animationSpec = tween(300)
                                )
                                if (swipeRight) onSwipeRight() else onSwipeLeft()
                            } else {
                                // Regresa al centro
                                launch { offsetX.animateTo(0f, tween(300)) }
                                launch { offsetY.animateTo(0f, tween(300)) }
                                launch { rotation.animateTo(0f, tween(300)) }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                            // Calcula rotación sutil dependiendo hacia donde va
                            rotation.snapTo(offsetX.value / 20)
                        }
                    }
                )
            },
        shape = RoundedCornerShape(24.dp),
        elevation = if (isTopCard) 8.dp else 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Simulamos foto grande usando color o ícono por el momento
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Foto de\n${pet.name}", fontSize = 32.sp, color = Color.White)
            }

            // Información sobrepuesta
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(24.dp)
            ) {
                Column {
                    Text(text = "${pet.name}, Raza: ${pet.breed ?: "N/A"}", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Especie: ${pet.species}", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Chips de temperamento
                    if (pet.temperament.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pet.temperament.take(3).forEach {
                                Surface(
                                    color = PrimaryPink,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(text = it, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
