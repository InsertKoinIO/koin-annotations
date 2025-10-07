package org.koin.sample.androidx

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import org.koin.core.option.viewModelScopeFactory
import org.koin.ksp.generated.startKoin
import org.koin.sample.androidx.di.MyKoinApp

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        MyKoinApp.startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MainApplication)
            options(
                viewModelScopeFactory()
            )
        }

//        startKoin {
//            androidLogger(Level.DEBUG)
//            androidContext(this@MainApplication)
//            modules(
////                defaultModule,
//                AppModule().module,
//            )
//            options(
//                viewModelScopeFactory()
//            )
//        }
    }
}