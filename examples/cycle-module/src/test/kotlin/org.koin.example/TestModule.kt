package org.koin.example

import org.junit.Test
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication
import org.koin.example.cycle.CycleModule
import org.koin.example.cycle.LazyCycleModule
import org.koin.ksp.generated.*

class TestModule {

    @Test
    fun testApp() {
        koinApplication {
            printLogger(Level.DEBUG)
            // else let's use our modules
            modules(
                CycleModule().module,
                LazyCycleModule().module,
            )
        }.koin
    }
}