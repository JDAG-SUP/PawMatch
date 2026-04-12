package com.pawmatch.app

import android.app.Application
import com.pawmatch.app.shared.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class PawMatchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@PawMatchApplication)
            modules(appModule)
        }
    }
}
