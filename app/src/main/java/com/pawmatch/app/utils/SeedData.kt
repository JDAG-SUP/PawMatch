package com.pawmatch.app.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.pawmatch.app.models.Pet
import com.pawmatch.app.models.User
import com.pawmatch.app.models.Match
import kotlinx.coroutines.tasks.await

suspend fun seedDatabaseIfEmpty(db: FirebaseFirestore, currentCity: String, currentUserPreference: String, currentUserId: String) {
    try {
        // Inject fake match unconditionally to guarantee the Matches Screen is populated
        if (currentUserId.isNotEmpty()) {
            val matchId = if (currentUserId < "mock_1") "${currentUserId}_mock_1" else "mock_1_${currentUserId}"
            val match = Match(id = matchId, userAId = currentUserId, userBId = "mock_1", timestamp = System.currentTimeMillis())
            db.collection("matches").document(matchId).set(match).await()
        }

        val mockValidation = db.collection("pets").document("mock_pet_1").get().await()
        if (mockValidation.exists()) {
            Log.d("SeedData", "Database mock already exists. Skipping.")
            return
        }

        Log.d("SeedData", "Seeding database with pristine mock data...")

        val mockProfiles = listOf(
            Pair(
                User(id = "mock_1", name = "Valeria", city = currentCity, bio = "Amante de los animales y la fotografía.", hobbies = "Fotografía, Senderismo", preferenceAnimalType = "Perro"),
                Pet(id = "mock_pet_1", ownerId = "mock_1", name = "Zeus", animalType = "Perro", breed = "Golden Retriever", age = "2 años", city = currentCity, shortDescription = "Juguetón, amigable y muy consentido. Adora los parques grandes.", imageUrls = listOf("https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=600&q=80"))
            ),
            Pair(
                User(id = "mock_2", name = "Andrés", city = currentCity, bio = "Siempre buscando aventuras con mi mejor amigo.", hobbies = "Montañismo", preferenceAnimalType = "Perro"),
                Pet(id = "mock_pet_2", ownerId = "mock_2", name = "Loki", animalType = "Perro", breed = "Husky Siberiano", age = "3 años", city = currentCity, shortDescription = "Muy activo. Busco compañeros para correr por las mañanas.", imageUrls = listOf("https://images.unsplash.com/photo-1605568427561-40dd23c2acea?auto=format&fit=crop&w=600&q=80"))
            ),
            Pair(
                User(id = "mock_3", name = "Camila", city = currentCity, bio = "Mi gato gobierna la casa. Fanática del café.", hobbies = "Lectura, Café", preferenceAnimalType = "Gato"),
                Pet(id = "mock_pet_3", ownerId = "mock_3", name = "Salem", animalType = "Gato", breed = "Bombay", age = "4 años", city = currentCity, shortDescription = "Domina el techo pero es súper tierno. Busco amiguitos tranquilos.", imageUrls = listOf("https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=600&q=80"))
            ),
            Pair(
                User(id = "mock_4", name = "Sebastián", city = currentCity, bio = "Vida saludable. Paseos largos en el parque.", hobbies = "Deportes, Naturaleza", preferenceAnimalType = currentUserPreference),
                Pet(id = "mock_pet_4", ownerId = "mock_4", name = "Kira", animalType = currentUserPreference, breed = if (currentUserPreference == "Perro") "Beagle" else "Siames", age = "1 año", city = currentCity, shortDescription = "Cachorra curiosa buscando amigos de juegos rápidos.", imageUrls = listOf("https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&w=600&q=80"))
            )
        )

        for (profile in mockProfiles) {
            db.collection("users").document(profile.first.id).set(profile.first).await()
            db.collection("pets").document(profile.second.id).set(profile.second).await()
        }

        // Inject fake match
        if (currentUserId.isNotEmpty()) {
            val matchId = if (currentUserId < "mock_1") "${currentUserId}_mock_1" else "mock_1_${currentUserId}"
            val match = Match(id = matchId, userAId = currentUserId, userBId = "mock_1", timestamp = System.currentTimeMillis())
            db.collection("matches").document(matchId).set(match).await()
        }
        
        Log.d("SeedData", "Seeding successful!")
    } catch (e: Exception) {
        Log.e("SeedData", "Error seeding database: ${e.message}")
    }
}
