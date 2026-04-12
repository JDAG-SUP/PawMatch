package com.pawmatch.app.shared.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    @SerialName("id") val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("bio") val bio: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("verified") val isVerified: Boolean? = false
)

@Serializable
data class PetDto(
    @SerialName("id") val id: String? = null, // Puede ser null al insertar
    @SerialName("owner_id") val ownerId: String? = null, // Lo maneja supabase / auth.uid() en lo posible, o lo pasamos explícito
    @SerialName("name") val name: String,
    @SerialName("species") val species: String,
    @SerialName("breed") val breed: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    @SerialName("size") val size: String? = null,
    @SerialName("temperament") val temperament: List<String> = emptyList(),
    @SerialName("photos") val photos: List<String> = emptyList(),
    @SerialName("bio") val bio: String? = null,
    @SerialName("vaccinated") val isVaccinated: Boolean? = false,
    @SerialName("neutered") val isNeutered: Boolean? = false,
    @SerialName("health_notes") val healthNotes: String? = null,
    @SerialName("looking_for") val lookingFor: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean? = true
)
