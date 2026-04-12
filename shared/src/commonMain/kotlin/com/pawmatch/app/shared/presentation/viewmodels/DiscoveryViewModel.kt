package com.pawmatch.app.shared.presentation.viewmodels

import com.pawmatch.app.shared.domain.entities.Pet
import com.pawmatch.app.shared.domain.repositories.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.pawmatch.app.shared.domain.repositories.MatchRepository

sealed class DiscoveryState {
    object Loading : DiscoveryState()
    data class Feed(val pets: List<Pet>, val myPetId: String) : DiscoveryState()
    data class Error(val message: String) : DiscoveryState()
}

class DiscoveryViewModel(
    private val petRepository: PetRepository,
    private val matchRepository: MatchRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<DiscoveryState>(DiscoveryState.Loading)
    val uiState: StateFlow<DiscoveryState> = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { DiscoveryState.Loading }
            
            // Requerimos nuestra mascota principal para efectuar Swipes
            val myPetsRes = petRepository.getMyPets()
            if (myPetsRes.isFailure || myPetsRes.getOrNull().isNullOrEmpty()) {
                _uiState.update { DiscoveryState.Error("Necesitas registrar una mascota para buscar.") }
                return@launch
            }
            val primaryPetId = myPetsRes.getOrNull()!!.first().id

            val result = petRepository.getDiscoverablePets()
            
            result.onSuccess { pets ->
                _uiState.update { DiscoveryState.Feed(pets, primaryPetId) }
            }.onFailure { err ->
                _uiState.update { DiscoveryState.Error(err.message ?: "Failed to load feed") }
            }
        }
    }

    fun swipePet(targetPetId: String, liked: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is DiscoveryState.Feed) {
                val updatedPets = currentState.pets.filter { it.id != targetPetId }
                _uiState.update { currentState.copy(pets = updatedPets) }
                
                // Registramos la acción usando el ID de nuestra mascota
                matchRepository.recordSwipe(currentState.myPetId, targetPetId, liked)
            }
        }
    }
}
