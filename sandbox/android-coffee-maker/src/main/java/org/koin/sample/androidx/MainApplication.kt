package org.koin.sample.androidx

import android.app.Application
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.example.di.CoffeeAppModule
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module
import org.koin.sample.androidx.app.AppModule
import org.koin.sample.androidx.data.DataModule
import org.koin.sample.androidx.repository.RepositoryModule

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            modules(
                defaultModule,
                DataModule().module,
                RepositoryModule().module,
                AppModule().module,
                CoffeeAppModule().module
            )
        }
    }
}