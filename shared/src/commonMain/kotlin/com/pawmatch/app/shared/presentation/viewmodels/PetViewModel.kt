package com.pawmatch.app.shared.presentation.viewmodels

import com.pawmatch.app.shared.domain.entities.Pet
import com.pawmatch.app.shared.domain.entities.UserProfile
import com.pawmatch.app.shared.domain.repositories.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PetViewModel(
    private val petRepository: PetRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<PetState>(PetState.Idle)
    val uiState: StateFlow<PetState> = _uiState.asStateFlow()

    fun completeOnboarding(
        displayName: String,
        bio: String,
        petName: String,
        species: String,
        breed: String
    ) {
        viewModelScope.launch {
            _uiState.update { PetState.Loading }

            // 1. Crear el Perfil del Usuario
            val profileResponse = petRepository.updateMyProfile(
                UserProfile(id = "", displayName = displayName, bio = bio)
            )

            if (profileResponse.isFailure) {
                _uiState.update { PetState.Error(profileResponse.exceptionOrNull()?.message ?: "Error al guardar perfil") }
                return@launch
            }

            // 2. Crear la primera mascota
            val petResponse = petRepository.addPet(
                Pet(
                    ownerId = "", // ownerId se asume en Base de Datos vía userId actual o el interceptor Postgrest
                    name = petName,
                    species = species,
                    breed = breed
                )
            )

            if (petResponse.isFailure) {
                _uiState.update { PetState.Error(petResponse.exceptionOrNull()?.message ?: "Error al registrar la mascota") }
                return@launch
            }

            _uiState.update { 
                PetState.Success(
                    profile = profileResponse.getOrNull(), 
                    pet = petResponse.getOrNull()
                ) 
            }
        }
    }
}
