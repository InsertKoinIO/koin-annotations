package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.native.PlatformComponentD
import com.jetbrains.kmpapp.native.PlatformComponentDiOS
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
actual class NativeModuleC

actual class ContextWrapper

@Module
@Configuration
actual class ContextModule {

    @Single
    actual fun providesContextWrapper(scope : Scope) : ContextWrapper = ContextWrapper()
}

@Module
actual class NativeModuleD {

    @Factory //dynamically passing scope to let resolve side components
    actual fun providesPlatformComponentD(ctx: ContextWrapper) : PlatformComponentD = PlatformComponentDiOS()
}