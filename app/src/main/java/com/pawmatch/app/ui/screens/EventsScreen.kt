package com.pawmatch.app.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class CommunityEvent(
    val id: String = "",
    val creatorId: String = "",
    val title: String = "",
    val date: String = "",
    val location: String = "",
    val maxAttendees: Int = 0,
    val currentAttendees: Int = 0,
    val imageUrl: String = "https://images.unsplash.com/photo-1544568100-847a948585b9?auto=format&fit=crop&w=600&q=80"
)

@Composable
fun EventsScreen() {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var eventsList by remember { mutableStateOf<List<CommunityEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showEventDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CommunityEvent?>(null) }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
        }
    }

    fun loadEvents() {
        coroutineScope.launch {
            try {
                val snapshot = db.collection("events").get().await()
                eventsList = snapshot.documents.mapNotNull { it.toObject(CommunityEvent::class.java) }
            } catch (e: Exception) {
                Log.e("EventsScreen", "Error loading events", e)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar Mock
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Eventos comunitarios", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier.size(40.dp).clickable { 
                    editingEvent = null
                    imageUri = null
                    showEventDialog = true 
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
        
        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha=0.1f))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (eventsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No hay eventos en tu área. ¡Anímate a crear uno!", style = MaterialTheme.typography.bodyLarge, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(eventsList) { event ->
                    EventCard(
                        event = event,
                        isOwner = event.creatorId == currentUserId,
                        onEdit = {
                            editingEvent = event
                            imageUri = null
                            showEventDialog = true
                        },
                        onDelete = {
                            coroutineScope.launch {
                                try {
                                    db.collection("events").document(event.id).delete().await()
                                    Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show()
                                    loadEvents()
                                } catch (e: Exception) {
                                    Log.e("EventsScreen", "Error deleting event", e)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showEventDialog) {
        var title by remember { mutableStateOf(editingEvent?.title ?: "") }
        var date by remember { mutableStateOf(editingEvent?.date ?: "") }
        var location by remember { mutableStateOf(editingEvent?.location ?: "") }
        var maxAttendees by remember { mutableStateOf(if (editingEvent != null) editingEvent!!.maxAttendees.toString() else "") }
        var isSaving by remember { mutableStateOf(false) }

        val isEditMode = editingEvent != null

        AlertDialog(
            onDismissRequest = { 
                if (!isSaving) {
                    showEventDialog = false 
                    editingEvent = null
                }
            },
            title = { Text(if (isEditMode) "Editar Evento" else "Crear Evento", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Título del evento")
                    OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true)
                    Text("Fecha y Hora")
                    OutlinedTextField(value = date, onValueChange = { date = it }, placeholder = { Text("Ej: 20 May, 16:00 PM") }, singleLine = true)
                    Text("Ubicación exacta")
                    OutlinedTextField(value = location, onValueChange = { location = it }, placeholder = { Text("Ej: Parque México") }, singleLine = true)
                    Text("Límite de asistentes")
                    OutlinedTextField(value = maxAttendees, onValueChange = { maxAttendees = it }, placeholder = { Text("Ej: 30") }, singleLine = true)
                    
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (imageUri != null) "Imagen seleccionada ✔️" else if(isEditMode) "Cambiar Imagen de Portada" else "Añadir Imagen de Portada")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && date.isNotBlank() && location.isNotBlank() && maxAttendees.isNotBlank()) {
                            coroutineScope.launch {
                                isSaving = true
                                try {
                                    val eventId = editingEvent?.id ?: UUID.randomUUID().toString()
                                    var finalImageUrl = editingEvent?.imageUrl ?: "https://images.unsplash.com/photo-1544568100-847a948585b9?auto=format&fit=crop&w=600&q=80"
                                    
                                    if (imageUri != null) {
                                        Toast.makeText(context, "Subiendo portada...", Toast.LENGTH_SHORT).show()
                                        val storageRef = FirebaseStorage.getInstance().reference.child("event_images/${eventId}_${System.currentTimeMillis()}.jpg")
                                        storageRef.putFile(imageUri!!).await()
                                        finalImageUrl = storageRef.downloadUrl.await().toString()
                                    }

                                    val eventInfo = CommunityEvent(
                                        id = eventId,
                                        creatorId = currentUserId,
                                        title = title,
                                        date = date,
                                        location = location,
                                        maxAttendees = maxAttendees.toIntOrNull() ?: 20,
                                        currentAttendees = editingEvent?.currentAttendees ?: 1,
                                        imageUrl = finalImageUrl
                                    )
                                    db.collection("events").document(eventId).set(eventInfo).await()
                                    Toast.makeText(context, if(isEditMode) "Evento actualizado" else "Evento publicado", Toast.LENGTH_SHORT).show()
                                    
                                    showEventDialog = false
                                    editingEvent = null
                                    imageUri = null
                                    loadEvents()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier=Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if(isEditMode) "Actualizar" else "Publicar")
                    }
                }
            },
            dismissButton = {
                if (!isSaving) {
                    TextButton(onClick = { showEventDialog = false; editingEvent = null }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }
}

@Composable
fun EventCard(event: CommunityEvent, isOwner: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isFull = event.currentAttendees >= event.maxAttendees

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isFull) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color(0xFFFF1744), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("LLENO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                
                if (isOwner) {
                     Row(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                         Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end=8.dp).clickable { onEdit() }
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Evento", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.clickable { onDelete() }
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Borrar Evento", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                     }
                }
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(event.date, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(event.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${event.currentAttendees} / ${event.maxAttendees} asistentes", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
    }
}
