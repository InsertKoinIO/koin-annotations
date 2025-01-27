package org.koin.sample.clients

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module(includes = [ClientModuleA::class, ClientModuleB::class])
class ClientModule

@Module
class ClientModuleA {

    @Single
    @Named("clientA")
    fun createClient() = HttpClient(CIO) {}
}

@Module
class ClientModuleB {

    @Single
    @Named("clientB")
    fun createClient() = HttpClient(CIO) {}
}