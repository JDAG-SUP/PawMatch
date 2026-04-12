package com.pawmatch.app.shared.domain.repositories

import com.pawmatch.app.shared.domain.entities.PetMatch
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    suspend fun recordSwipe(myPetId: String, targetPetId: String, liked: Boolean): Result<Unit>
    suspend fun getMatchesForPet(petId: String): Result<List<PetMatch>>
    
    // Escucha en tiempo real de nuevos Matches en una mascota local
    fun observeNewMatches(petId: String): Flow<PetMatch>
}
