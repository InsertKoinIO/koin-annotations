package org.koin.sample.androidx

import android.app.Application
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.example.di.CoffeeAppModule
import org.koin.example.di.CoffeeTesterModule
import org.koin.example.service.ServiceModule
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module
import org.koin.sample.androidx.di.AppModule
import org.koin.sample.androidx.di.DataModule
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
                CoffeeAppModule().module,
                ServiceModule().module,
                CoffeeTesterModule().module,
            )
        }
    }
}