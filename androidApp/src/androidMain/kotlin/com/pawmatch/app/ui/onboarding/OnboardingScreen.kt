package com.pawmatch.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawmatch.app.shared.presentation.viewmodels.PetState
import com.pawmatch.app.shared.presentation.viewmodels.PetViewModel
import com.pawmatch.app.ui.theme.PrimaryPink
import org.koin.compose.koinInject

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: PetViewModel = koinInject(),
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableStateOf(1) }

    // User inputs
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    // Pet inputs
    var petName by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is PetState.Success) {
            onFinish()
        }
    }

    Scaffold(
        backgroundColor = MaterialTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = currentStep / 2f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                color = PrimaryPink,
                backgroundColor = Color.LightGray
            )

            AnimatedContent(targetState = currentStep, label = "OnboardingSteps") { step ->
                when (step) {
                    1 -> UserProfileStep(
                        displayName = displayName,
                        onNameChange = { displayName = it },
                        bio = bio,
                        onBioChange = { bio = it },
                        onNext = { currentStep = 2 }
                    )
                    2 -> PetProfileStep(
                        petName = petName,
                        onPetNameChange = { petName = it },
                        species = species,
                        onSpeciesChange = { species = it },
                        breed = breed,
                        onBreedChange = { breed = it },
                        isLoading = uiState is PetState.Loading,
                        errorMsg = (uiState as? PetState.Error)?.message,
                        onSubmit = {
                            viewModel.completeOnboarding(
                                displayName = displayName,
                                bio = bio,
                                petName = petName,
                                species = species,
                                breed = breed
                            )
                        },
                        onBack = { currentStep = 1 }
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileStep(
    displayName: String,
    onNameChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column {
        Text(
            text = "¡Hablemos de ti!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Agrega tu información básica para que otros dueños te conozcan.",
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = onNameChange,
            label = { Text("Tu nombre o apodo") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            label = { Text("Bio (Opcional)") },
            placeholder = { Text("¡Me encanta salir a correr!") },
            modifier = Modifier.fillMaxWidth().height(120.dp).padding(bottom = 32.dp),
            shape = RoundedCornerShape(12.dp),
            maxLines = 4
        )

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryPink),
            enabled = displayName.isNotBlank()
        ) {
            Text("Siguiente", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PetProfileStep(
    petName: String,
    onPetNameChange: (String) -> Unit,
    species: String,
    onSpeciesChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    isLoading: Boolean,
    errorMsg: String?,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column {
        Text(
            text = "Y ahora, tu mejor amigo 🐾",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Crea el perfil de tu primera mascota.",
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = petName,
            onValueChange = onPetNameChange,
            label = { Text("Nombre de la mascota") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = species,
            onValueChange = onSpeciesChange,
            label = { Text("Especie (e.g. Perro, Gato)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = breed,
            onValueChange = onBreedChange,
            label = { Text("Raza") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp)
        )

        if (errorMsg != null) {
            Text(text = errorMsg, color = Color.Red, modifier = Modifier.padding(bottom = 16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Volver", color = PrimaryPink)
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryPink),
                enabled = petName.isNotBlank() && species.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Finalizar", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
