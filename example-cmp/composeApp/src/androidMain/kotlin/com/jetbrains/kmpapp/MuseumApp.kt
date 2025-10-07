package com.jetbrains.kmpapp

import android.app.Application
import com.jetbrains.kmpapp.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class MuseumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
//            androidLogger()
            androidContext(this@MuseumApp)
        }
    }
}
