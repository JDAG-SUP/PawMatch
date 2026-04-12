package com.pawmatch.app.shared.presentation.viewmodels

import com.pawmatch.app.shared.domain.entities.Pet
import com.pawmatch.app.shared.domain.entities.UserProfile

sealed class PetState {
    object Idle : PetState()
    object Loading : PetState()
    data class Success(val profile: UserProfile? = null, val pet: Pet? = null) : PetState()
    data class Error(val message: String) : PetState()
}
