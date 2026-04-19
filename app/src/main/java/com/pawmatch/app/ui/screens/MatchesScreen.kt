package com.pawmatch.app.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pawmatch.app.models.Match
import com.pawmatch.app.models.Pet
import com.pawmatch.app.models.User
import kotlinx.coroutines.tasks.await

data class MatchDisplayData(
    val matchedUserId: String,
    val matchedUser: User,
    val matchedPet: Pet?
)

@Composable
fun MatchesScreen(onNavigateToPublicProfile: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: return

    var matchesList by remember { mutableStateOf<List<MatchDisplayData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        try {
            // Firestore doesn't have an easy OR query across different fields in basic SDK without Filter API. 
            // So we'll run two queries for the MVP.
            val queryA = db.collection("matches").whereEqualTo("userAId", currentUserId).get().await()
            val queryB = db.collection("matches").whereEqualTo("userBId", currentUserId).get().await()
            
            val allMatches = (queryA.documents + queryB.documents)
                .mapNotNull { it.toObject(Match::class.java) }
                .distinctBy { it.id }

            val displayData = mutableListOf<MatchDisplayData>()

            for (match in allMatches) {
                val targetUserId = if (match.userAId == currentUserId) match.userBId else match.userAId
                val targetUserDoc = db.collection("users").document(targetUserId).get().await()
                val targetUser = targetUserDoc.toObject(User::class.java)

                if (targetUser != null) {
                    val targetPetQuery = db.collection("pets").whereEqualTo("ownerId", targetUserId).get().await()
                    val targetPet = targetPetQuery.documents.firstOrNull()?.toObject(Pet::class.java)
                    
                    displayData.add(MatchDisplayData(targetUserId, targetUser, targetPet))
                }
            }
            matchesList = displayData
            
        } catch (e: Exception) {
            Log.e("MatchesScreen", "Error loading matches", e)
        } finally {
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tus Matches \uD83D\uDC96", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (matchesList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes matches. ¡Ve a deslizar!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn {
                items(matchesList) { matchData ->
                    MatchItemRow(matchData = matchData, onClick = {
                        onNavigateToPublicProfile(matchData.matchedUserId)
                    })
                }
            }
        }
    }
}

@Composable
fun MatchItemRow(matchData: MatchDisplayData, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (matchData.matchedPet != null && matchData.matchedPet.imageUrls.isNotEmpty()) {
               AsyncImage(
                   model = matchData.matchedPet.imageUrls.first(),
                   contentDescription = "Pet Image",
                   contentScale = ContentScale.Crop,
                   modifier = Modifier.size(64.dp).clip(CircleShape)
               )
            } else {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                    Text("\uD83D\uDC3E") // Emoji Huella
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = "Dueño: ${matchData.matchedUser.name}", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Mascota: ${matchData.matchedPet?.name ?: "Desconocido"} (${matchData.matchedPet?.breed ?: "..."})", 
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
