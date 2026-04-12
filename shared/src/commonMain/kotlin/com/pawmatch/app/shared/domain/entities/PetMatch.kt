package com.pawmatch.app.shared.domain.entities

data class PetMatch(
    val matchId: String,
    val myPetId: String,
    val matchedPetId: String,
    val createdAt: String,
    // La entidad de negocio opcionalmente puede pre-cargar la tarjeta del perro matcheado para la UI
    val matchedPetInfo: Pet? = null 
)
