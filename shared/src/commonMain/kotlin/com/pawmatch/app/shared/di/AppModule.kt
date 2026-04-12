package com.pawmatch.app.shared.di

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import org.koin.core.module.Module
import org.koin.dsl.module
import com.pawmatch.app.shared.domain.repositories.AuthRepository
import com.pawmatch.app.shared.data.remote.SupabaseAuthDataSource
import com.pawmatch.app.shared.data.repositories.AuthRepositoryImpl
import com.pawmatch.app.shared.presentation.viewmodels.AuthViewModel
import com.pawmatch.app.shared.data.remote.SupabasePetDataSource
import com.pawmatch.app.shared.data.repositories.PetRepositoryImpl
import com.pawmatch.app.shared.domain.repositories.PetRepository
import com.pawmatch.app.shared.presentation.viewmodels.PetViewModel
import com.pawmatch.app.shared.presentation.viewmodels.DiscoveryViewModel
import com.pawmatch.app.shared.presentation.viewmodels.ProfileViewModel
import com.pawmatch.app.shared.domain.repositories.MatchRepository
import com.pawmatch.app.shared.data.repositories.MatchRepositoryImpl
import com.pawmatch.app.shared.data.remote.SupabaseMatchDataSource
import com.pawmatch.app.shared.presentation.viewmodels.MatchViewModel

val appModule: Module = module {
    single {
        createSupabaseClient(
            supabaseUrl = "YOUR_SUPABASE_URL",
            supabaseKey = "YOUR_SUPABASE_ANON_KEY"
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
    
    // Auth Data
    single { SupabaseAuthDataSource(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    
    // Pet Data
    single { SupabasePetDataSource(get()) }
    single<PetRepository> { PetRepositoryImpl(get()) }
    
    // Match Data
    single { SupabaseMatchDataSource(get()) }
    single<MatchRepository> { MatchRepositoryImpl(get(), get()) }
    
    // ViewModels
    factory { AuthViewModel(get()) }
    factory { PetViewModel(get()) }
    factory { DiscoveryViewModel(get(), get()) }
    factory { ProfileViewModel(get()) }
    factory { MatchViewModel(get(), get()) }
}
