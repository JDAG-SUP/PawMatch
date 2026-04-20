package com.pawmatch.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (currentStep) {
            0 -> OnboardingSlide(
                title = "Encuentra el compañero\nperfecto",
                subtitle = "Conecta con otros dueños de mascotas cerca de ti.",
                isFirst = true,
                onNext = { currentStep = 1 }
            )
            1 -> OnboardingSlide(
                title = "Crea tu perfil ahora",
                subtitle = "Es fácil, rápido y pensado para la felicidad de tu mascota.",
                isFirst = false,
                onNext = { currentStep = 2 }
            )
            2 -> LoginForm(onAuthSuccess)
        }
    }
}

@Composable
fun OnboardingSlide(title: String, subtitle: String, isFirst: Boolean, onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // Circular Icon
        Box(
            modifier = Modifier.size(200.dp).background(Color(0xFFE5E2D9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(80.dp))
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = subtitle, style = MaterialTheme.typography.bodyLarge, color = Color.Gray, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp))

        // Pager Dots
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
             Box(modifier = Modifier.height(8.dp).width(if(isFirst) 24.dp else 8.dp).background(if(isFirst) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape))
             Box(modifier = Modifier.height(8.dp).width(8.dp).background(Color.LightGray, CircleShape))
             Box(modifier = Modifier.height(8.dp).width(if(!isFirst) 24.dp else 8.dp).background(if(!isFirst) MaterialTheme.colorScheme.primary else Color.LightGray, CircleShape))
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isFirst) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Siguiente", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Crear cuenta", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp).background(Color.White, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                border = null
            ) {
                Text("Iniciar sesión", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LoginForm(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Ingresa a PetMatch", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) return@Button
                isLoading = true
                val auth = FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onAuthSuccess()
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { createTask ->
                                    isLoading = false
                                    if (createTask.isSuccessful) {
                                        onAuthSuccess()
                                    } else {
                                        Toast.makeText(context, "Error: ${createTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) 
            else Text("Continuar", style = MaterialTheme.typography.titleMedium)
        }
    }
}
