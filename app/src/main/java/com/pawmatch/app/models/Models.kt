package com.pawmatch.app.models

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val city: String = "", // Usará una lista predefinida en la UI para evitar problemas
    val whatsappNumber: String = "",
    val bio: String = "",
    val hobbies: String = "",
    val preferenceAnimalType: String = "" // e.g. "Perro", "Gato", "Ave"
)

data class Pet(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val animalType: String = "",
    val breed: String = "",
    val age: String = "",
    val city: String = "", // Se heredará del dueño y servirá de filtro local
    val shortDescription: String = "",
    val imageUrls: List<String> = emptyList()
)

data class Match(
    val id: String = "",
    val userAId: String = "",
    val userBId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
