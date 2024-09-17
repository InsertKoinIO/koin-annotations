package org.koin.sample.androidx

import RootModule
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.ksp.generated.module
import org.koin.sample.androidx.di.AppModule

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MainApplication)
            modules(
//                defaultModule,
                AppModule().module,
                RootModule().module
            )
        }
    }
}