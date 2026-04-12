package com.pawmatch.app.shared.domain.repositories

import com.pawmatch.app.shared.domain.entities.Pet
import com.pawmatch.app.shared.domain.entities.UserProfile

interface PetRepository {
    suspend fun getMyProfile(): Result<UserProfile>
    suspend fun updateMyProfile(profile: UserProfile): Result<UserProfile>

    suspend fun getMyPets(): Result<List<Pet>>
    suspend fun addPet(pet: Pet): Result<Pet>
    suspend fun updatePet(pet: Pet): Result<Pet>
    suspend fun deletePet(petId: String): Result<Unit>
    
    suspend fun uploadPetPhoto(petId: String, photoBytes: ByteArray, fileExtension: String): Result<String>

    suspend fun getDiscoverablePets(): Result<List<Pet>>
}
