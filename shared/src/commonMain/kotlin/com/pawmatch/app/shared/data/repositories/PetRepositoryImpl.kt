package com.pawmatch.app.shared.data.repositories

import com.pawmatch.app.shared.data.remote.SupabasePetDataSource
import com.pawmatch.app.shared.data.remote.dto.PetDto
import com.pawmatch.app.shared.data.remote.dto.UserProfileDto
import com.pawmatch.app.shared.domain.entities.Pet
import com.pawmatch.app.shared.domain.entities.UserProfile
import com.pawmatch.app.shared.domain.repositories.PetRepository

class PetRepositoryImpl(
    private val petDataSource: SupabasePetDataSource
) : PetRepository {

    override suspend fun getMyProfile(): Result<UserProfile> = runCatching {
        petDataSource.getProfile().toDomain()
    }

    override suspend fun updateMyProfile(profile: UserProfile): Result<UserProfile> = runCatching {
        petDataSource.updateProfile(profile.toDto()).toDomain()
    }

    override suspend fun getMyPets(): Result<List<Pet>> = runCatching {
        petDataSource.getMyPets().map { it.toDomain() }
    }

    override suspend fun addPet(pet: Pet): Result<Pet> = runCatching {
        petDataSource.insertPet(pet.toDto()).toDomain()
    }

    override suspend fun updatePet(pet: Pet): Result<Pet> = runCatching {
        petDataSource.updatePet(pet.toDto()).toDomain()
    }

    override suspend fun deletePet(petId: String): Result<Unit> = runCatching {
        petDataSource.deletePet(petId)
    }

    override suspend fun uploadPetPhoto(petId: String, photoBytes: ByteArray, fileExtension: String): Result<String> = runCatching {
        petDataSource.uploadPetPhoto(petId, photoBytes, fileExtension)
    }

    // --- Mappers ---
    
    private fun UserProfileDto.toDomain() = UserProfile(
        id = id,
        displayName = displayName,
        bio = bio,
        avatarUrl = avatarUrl,
        phone = phone,
        isVerified = isVerified ?: false
    )

    private fun UserProfile.toDto() = UserProfileDto(
        id = id,
        displayName = displayName,
        bio = bio,
        avatarUrl = avatarUrl,
        phone = phone,
        isVerified = isVerified
    )

    private fun PetDto.toDomain() = Pet(
        id = id ?: "",
        ownerId = ownerId ?: "",
        name = name,
        species = species,
        breed = breed,
        gender = gender,
        birthDate = birthDate,
        size = size,
        temperament = temperament,
        photos = photos,
        bio = bio,
        isVaccinated = isVaccinated ?: false,
        isNeutered = isNeutered ?: false,
        healthNotes = healthNotes,
        lookingFor = lookingFor,
        isActive = isActive ?: true
    )

    private fun Pet.toDto() = PetDto(
        id = if (id.isBlank()) null else id,
        ownerId = ownerId,
        name = name,
        species = species,
        breed = breed,
        gender = gender,
        birthDate = birthDate,
        size = size,
        temperament = temperament,
        photos = photos,
        bio = bio,
        isVaccinated = isVaccinated,
        isNeutered = isNeutered,
        healthNotes = healthNotes,
        lookingFor = lookingFor,
        isActive = isActive
    )
}
