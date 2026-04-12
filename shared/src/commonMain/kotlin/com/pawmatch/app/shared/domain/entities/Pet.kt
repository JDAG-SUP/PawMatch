package com.pawmatch.app.shared.domain.entities

data class Pet(
    val id: String = "",
    val ownerId: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val size: String? = null,
    val temperament: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val bio: String? = null,
    val isVaccinated: Boolean = false,
    val isNeutered: Boolean = false,
    val healthNotes: String? = null,
    val lookingFor: List<String> = emptyList(),
    val isActive: Boolean = true
)
