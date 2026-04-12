package com.pawmatch.app.shared.data.repositories

import com.pawmatch.app.shared.data.remote.SupabaseMatchDataSource
import com.pawmatch.app.shared.data.remote.SupabasePetDataSource
import com.pawmatch.app.shared.data.remote.dto.SwipeDto
import com.pawmatch.app.shared.domain.entities.PetMatch
import com.pawmatch.app.shared.domain.repositories.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MatchRepositoryImpl(
    private val matchDataSource: SupabaseMatchDataSource,
    private val petDataSource: SupabasePetDataSource
) : MatchRepository {

    override suspend fun recordSwipe(myPetId: String, targetPetId: String, liked: Boolean): Result<Unit> = runCatching {
        matchDataSource.recordSwipe(
            SwipeDto(
                swiperPetId = myPetId,
                targetPetId = targetPetId,
                liked = liked
            )
        )
    }

    override suspend fun getMatchesForPet(petId: String): Result<List<PetMatch>> = runCatching {
        val matches = matchDataSource.getMatchesForPet(petId)
        matches.map { match ->
            // El target pet es el ID que NO es myPetId
            val matchedPetId = if (match.pet1Id == petId) match.pet2Id else match.pet1Id
            
            PetMatch(
                matchId = match.id,
                myPetId = petId,
                matchedPetId = matchedPetId,
                createdAt = match.createdAt
            )
        }
    }

    override fun observeNewMatches(petId: String): Flow<PetMatch> {
        return matchDataSource.observeNewMatches(petId).map { match ->
            val matchedPetId = if (match.pet1Id == petId) match.pet2Id else match.pet1Id
            PetMatch(
                matchId = match.id,
                myPetId = petId,
                matchedPetId = matchedPetId,
                createdAt = match.createdAt
            )
        }
    }
}
