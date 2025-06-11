package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.native.PlatformComponentD
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.scope.Scope

@Module
actual class NativeModuleC

@Module
actual class NativeModuleD {

    @Factory
    actual fun providesPlatformComponentD(scope: Scope) : PlatformComponentD = PlatformComponentD(
        scope
    )
}