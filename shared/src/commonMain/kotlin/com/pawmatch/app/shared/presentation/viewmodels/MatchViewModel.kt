package com.pawmatch.app.shared.presentation.viewmodels

import com.pawmatch.app.shared.domain.entities.PetMatch
import com.pawmatch.app.shared.domain.repositories.MatchRepository
import com.pawmatch.app.shared.domain.repositories.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MatchesState(
    val isLoading: Boolean = true,
    val matches: List<PetMatch> = emptyList(),
    val error: String? = null
)

class MatchViewModel(
    private val matchRepository: MatchRepository,
    private val petRepository: PetRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MatchesState())
    val uiState: StateFlow<MatchesState> = _uiState.asStateFlow()

    private var activePetId: String? = null

    init {
        loadMyMatches()
    }

    fun loadMyMatches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Asumimos que carga los matches para tu masceota principal
            val petsRes = petRepository.getMyPets()
            if (petsRes.isSuccess && petsRes.getOrNull()?.isNotEmpty() == true) {
                activePetId = petsRes.getOrNull()!!.first().id
                
                val matchesRes = matchRepository.getMatchesForPet(activePetId!!)
                if (matchesRes.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, matches = matchesRes.getOrNull()!!) }
                    
                    // Iniciamos el tracker de Realtime
                    subscribeToRealtimeMatches()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = matchesRes.exceptionOrNull()?.message) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Se requiere una mascota para ver matches") }
            }
        }
    }

    private fun subscribeToRealtimeMatches() {
        val petId = activePetId ?: return
        viewModelScope.launch {
            matchRepository.observeNewMatches(petId).collect { newMatch ->
                // Actualiza la UI reactivamente si hubo match nuevo mientras tienes la app abierta
                val currentMatches = _uiState.value.matches
                if (currentMatches.none { it.matchId == newMatch.matchId }) {
                    _uiState.update { 
                        it.copy(matches = listOf(newMatch) + currentMatches)
                    }
                }
            }
        }
    }
}
