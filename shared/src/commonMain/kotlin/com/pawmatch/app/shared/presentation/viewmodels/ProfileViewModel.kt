package com.pawmatch.app.shared.presentation.viewmodels

import com.pawmatch.app.shared.domain.entities.Pet
import com.pawmatch.app.shared.domain.entities.UserProfile
import com.pawmatch.app.shared.domain.repositories.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileScreenState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val myPets: List<Pet> = emptyList(),
    val error: String? = null
)

class ProfileViewModel(
    private val petRepository: PetRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProfileScreenState(isLoading = true))
    val uiState: StateFlow<ProfileScreenState> = _uiState.asStateFlow()

    init {
        loadMyProfile()
    }

    fun loadMyProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val profileRes = petRepository.getMyProfile()
            val petsRes = petRepository.getMyPets()

            if (profileRes.isSuccess && petsRes.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        profile = profileRes.getOrNull(),
                        myPets = petsRes.getOrNull() ?: emptyList()
                    ) 
                }
            } else {
                val errMsg = profileRes.exceptionOrNull()?.message ?: petsRes.exceptionOrNull()?.message
                _uiState.update { it.copy(isLoading = false, error = errMsg) }
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            val result = petRepository.deletePet(petId)
            if (result.isSuccess) {
                // Refrescar lista local
                val updatedList = _uiState.value.myPets.filter { it.id != petId }
                _uiState.update { it.copy(myPets = updatedList) }
            }
        }
    }
}
