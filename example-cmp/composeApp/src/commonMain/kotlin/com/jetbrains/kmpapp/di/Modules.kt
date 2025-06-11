package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.native.PlatformComponentB
import com.jetbrains.kmpapp.native.PlatformComponentD
import com.jetbrains.kmpapp.screens.ViewModelModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@ComponentScan("com.jetbrains.kmpapp.data")
class DataModule {

    @Single
    fun json() = Json { ignoreUnknownKeys = true }

    @Single
    fun httpClient(json : Json) = HttpClient {
        install(ContentNegotiation) {
            json(json, contentType = ContentType.Any)
        }
    }
}

@Module(includes = [DataModule::class,ViewModelModule::class, NativeModuleA::class, NativeModuleB::class, NativeModuleC::class, NativeModuleD::class])
class AppModule

@Module
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
// No def in commonMain - let scan in native

@Module
class NativeModuleD() {

    @Factory //dynamically passing scope to let resolve side components
    fun providesPlatformComponentD(scope: Scope) : PlatformComponentD = PlatformComponentD(scope)
}