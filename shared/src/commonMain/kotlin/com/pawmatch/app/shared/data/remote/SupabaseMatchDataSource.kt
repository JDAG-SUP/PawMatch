package com.pawmatch.app.shared.data.remote

import com.pawmatch.app.shared.data.remote.dto.MatchDto
import com.pawmatch.app.shared.data.remote.dto.SwipeDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class SupabaseMatchDataSource(
    private val supabaseClient: SupabaseClient
) {
    suspend fun recordSwipe(swipe: SwipeDto) {
        supabaseClient.postgrest["swipes"].insert(swipe)
    }

    suspend fun getMatchesForPet(petId: String): List<MatchDto> {
        val matches1 = supabaseClient.postgrest["matches"]
            .select { filter { eq("pet1_id", petId) } }
            .decodeList<MatchDto>()
            
        val matches2 = supabaseClient.postgrest["matches"]
            .select { filter { eq("pet2_id", petId) } }
            .decodeList<MatchDto>()
            
        return matches1 + matches2
    }

    fun observeNewMatches(petId: String): Flow<MatchDto> {
        val channel = supabaseClient.channel("public:matches")
        
        // Creamos un flow de inserciones en la tabla matches
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "matches"
        }
        
        // Suscribirse asíncronamente al channel si no lo está (manejo corrutinas internamente)
        // en esta abstracción, el flow inicia la sub cuando lo recogen
        
        return changeFlow.mapNotNull { action ->
            val record = action.record
            // Mapeamos el JSON dinámico usando kotlinx.serialization que Supabase provee en action.decodeRecord() o iterando
            val matchDto = action.decodeRecord<MatchDto>()
            
            // Fíltralo solo si aplica a nosotros, por si acaso
            if (matchDto.pet1Id == petId || matchDto.pet2Id == petId) {
                matchDto
            } else {
                null
            }
        }
    }
}
