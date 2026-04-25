package com.pawmatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.pawmatch.app.ui.theme.LocalThemeManager

@Composable
fun SettingsScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToMatchingPrefs: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUid = auth.currentUser?.uid
    val isDarkThemeState = LocalThemeManager.current
    var isDarkMode by isDarkThemeState

    var userName by remember { mutableStateOf("Cargando...") }
    var petName by remember { mutableStateOf("...") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    DisposableEffect(currentUid) {
        if (currentUid == null) return@DisposableEffect onDispose {}

        val userListener = db.collection("users").document(currentUid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                userName = snapshot.getString("name")?.takeIf { it.isNotBlank() } ?: "Usuario"
            }
        }
        
        val petListener = db.collection("pets").whereEqualTo("ownerId", currentUid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                val petDoc = snapshot.documents.first()
                petName = petDoc.getString("name")?.takeIf { it.isNotBlank() } ?: "Mascota"
                val urls = petDoc.get("imageUrls") as? List<*>
                if (urls != null && urls.isNotEmpty()) {
                    profileImageUrl = urls[0] as? String
                }
            }
        }
        
        onDispose {
            userListener.remove()
            petListener.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp)) {
            Text("Perfil", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }

        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.1f))

        // Profile Section
        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = profileImageUrl ?: "https://images.unsplash.com/photo-1544568100-847a948585b9?auto=format&fit=crop&w=300&q=80",
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.LightGray)
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(4.dp).size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("S", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (petName == "...") userName else "$userName & $petName", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold, 
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(auth.currentUser?.email ?: "sofia@example.com", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToEditProfile,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth(0.5f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Editar perfiles")
            }
        }

        // Options List
        Column(modifier = Modifier.padding(24.dp)) {
            Text("AJUSTES DE CUENTA", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column {
                    SettingsRow(icon = Icons.Default.Settings, title = "Preferencias de Matching", iconColor = MaterialTheme.colorScheme.primary, onClick = onNavigateToMatchingPrefs)
                    Divider(modifier = Modifier.padding(start=56.dp), color = MaterialTheme.colorScheme.background)
                    SettingsRow(icon = Icons.Default.Notifications, title = "Notificaciones", iconColor = MaterialTheme.colorScheme.primary, onClick = {})
                    Divider(modifier = Modifier.padding(start=56.dp), color = MaterialTheme.colorScheme.background)
                    SettingsRow(icon = Icons.Default.Security, title = "Privacidad y Seguridad", iconColor = MaterialTheme.colorScheme.primary, onClick = onNavigateToPrivacy)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("APARIENCIA", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Modo Oscuro", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.surface, checkedTrackColor = MaterialTheme.colorScheme.primary, uncheckedThumbColor = MaterialTheme.colorScheme.surface, uncheckedTrackColor = Color(0xFFE0E0E0))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.clickable { auth.signOut() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Cerrar Sesión", style = MaterialTheme.typography.bodyLarge, color = Color.Red, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}
