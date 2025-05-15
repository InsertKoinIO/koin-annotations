package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.native.PlatformComponentB
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
actual class NativeModuleB {
    @Factory
    actual fun providesPlatformComponentB() = PlatformComponentB()
}

@Module
actual class NativeModuleC actual constructor()