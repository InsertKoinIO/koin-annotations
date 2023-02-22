package org.koin.example

import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.example.`interface`.MyInterfaceExt
import org.koin.example.newmodule.MyModule
import org.koin.example.newmodule.MyOtherComponent2
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module

class TestModule {

    @Test
    fun testApp() {
        val koin = startKoin {
            printLogger(Level.DEBUG)
            // else let's use our modules
            modules(
                defaultModule, MyModule().module
            )
        }.koin

        koin.get<MyInterfaceExt>()
        koin.get<MyOtherComponent>()
        koin.get<MyOtherComponent2>()
    }
}