package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.native.PlatformComponentB
import com.jetbrains.kmpapp.native.PlatformComponentD
import com.jetbrains.kmpapp.screens.ViewModelModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module(includes = [DataModule::class,ViewModelModule::class, NativeModuleA::class, NativeModuleB::class, NativeModuleC::class, NativeModuleD::class])
class AppModule

@Module(includes = [ContextModule::class])
@ComponentScan("com.jetbrains.kmpapp.native")
class NativeModuleA()
// Def is Tagged in CommonMain

@Module
class NativeModuleB() {

    @Factory
    fun providesPlatformComponentB() : PlatformComponentB = PlatformComponentB()
}
// Def is Tagged in CommonMain

@Module
expect class NativeModuleC()
// No def in commonMain - def are in natives (no constraints)

expect class ContextWrapper

@Module
expect class ContextModule() {

    @Single
    fun providesContextWrapper(scope : Scope) : ContextWrapper
}

@Module(includes = [ContextModule::class])
expect class NativeModuleD() {

    @Factory //dynamically passing scope to let resolve side components
    fun providesPlatformComponentD(ctx : ContextWrapper) : PlatformComponentD
}