package com.pawmatch.app.shared.data.remote

import com.pawmatch.app.shared.data.remote.dto.PetDto
import com.pawmatch.app.shared.data.remote.dto.UserProfileDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage

class SupabasePetDataSource(
    private val supabaseClient: SupabaseClient
) {
    private val userId: String
        get() = supabaseClient.auth.currentUserOrNull()?.id 
            ?: throw Exception("Unauthenticated")

    suspend fun getProfile(): UserProfileDto {
        return supabaseClient.postgrest["users_profiles"]
            .select { filter { eq("id", userId) } }
            .decodeSingle<UserProfileDto>()
    }

    suspend fun updateProfile(profile: UserProfileDto): UserProfileDto {
        return supabaseClient.postgrest["users_profiles"]
            .update(profile) { filter { eq("id", userId) } }
            .decodeSingle<UserProfileDto>()
    }

    suspend fun getMyPets(): List<PetDto> {
        return supabaseClient.postgrest["pets"]
            .select { filter { eq("owner_id", userId) } }
            .decodeList<PetDto>()
    }

    suspend fun insertPet(pet: PetDto): PetDto {
        val petWithOwner = pet.copy(ownerId = userId)
        return supabaseClient.postgrest["pets"]
            .insert(petWithOwner) { select() }
            .decodeSingle<PetDto>()
    }

    suspend fun updatePet(pet: PetDto): PetDto {
        return supabaseClient.postgrest["pets"]
            .update(pet) { 
                filter { 
                    eq("id", pet.id ?: throw Exception("Pet ID vacio al actualizar")) 
                    eq("owner_id", userId)
                } 
            }
            .decodeSingle<PetDto>()
    }

    suspend fun deletePet(petId: String) {
        supabaseClient.postgrest["pets"]
            .delete { 
                filter { 
                    eq("id", petId)
                    eq("owner_id", userId)
                } 
            }
    }

    suspend fun uploadPetPhoto(petId: String, photoBytes: ByteArray, extension: String): String {
        val bucket = supabaseClient.storage["pet-photos"]
        val fileName = "$userId/$petId/photo_${userId}_${kotlin.random.Random.nextInt()}.$extension"
        
        bucket.upload(fileName, photoBytes, upsert = true)
        return "${supabaseClient.supabaseUrl}/storage/v1/object/public/pet-photos/$fileName"
    }

    suspend fun getDiscoverablePets(): List<PetDto> {
        return supabaseClient.postgrest["pets"]
            .select { filter { neq("owner_id", userId) } }
            .decodeList<PetDto>()
    }
}
