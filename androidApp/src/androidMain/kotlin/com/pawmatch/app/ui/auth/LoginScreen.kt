package com.pawmatch.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pawmatch.app.shared.presentation.viewmodels.AuthState
import com.pawmatch.app.shared.presentation.viewmodels.AuthViewModel
import com.pawmatch.app.ui.theme.PrimaryPink
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = koinInject(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Redirigir si está autenticado
    LaunchedEffect(uiState) {
        if (uiState is AuthState.Authenticated) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Bienvenido a",
                fontSize = 20.sp,
                color = MaterialTheme.colors.onBackground
            )
            Text(
                text = "PawMatch",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryPink,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryPink,
                    cursorColor = PrimaryPink
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = PrimaryPink,
                    cursorColor = PrimaryPink
                )
            )

            if (uiState is AuthState.Error) {
                Text(
                    text = (uiState as AuthState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { viewModel.signIn(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryPink),
                enabled = email.isNotBlank() && password.isNotBlank() && uiState !is AuthState.Loading
            ) {
                if (uiState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Iniciar Sesión", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.signUp(email, password) },
                enabled = email.isNotBlank() && password.isNotBlank() && uiState !is AuthState.Loading
            ) {
                Text(text = "¿No tienes cuenta? Regístrate aquí", color = MaterialTheme.colors.onBackground)
            }
        }
    }
}
