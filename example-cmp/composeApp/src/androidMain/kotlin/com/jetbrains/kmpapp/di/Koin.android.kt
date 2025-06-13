package com.jetbrains.kmpapp.di

import android.content.Context
import com.jetbrains.kmpapp.native.PlatformComponentD
import com.jetbrains.kmpapp.native.PlatformComponentDAndroid
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@ComponentScan("com.jetbrains.kmpapp.other.android")
actual class NativeModuleC

actual class ContextWrapper(val context: Context)

@Module
actual class ContextModule {

    @Single
    actual fun providesContextWrapper(scope : Scope) : ContextWrapper = ContextWrapper(scope.get())
}

@Module(includes = [ContextModule::class])
actual class NativeModuleD {

    @Factory //dynamically passing scope to let resolve side components
    actual fun providesPlatformComponentD(ctx: ContextWrapper) : PlatformComponentD = PlatformComponentDAndroid(ctx)
}