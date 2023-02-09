package org.koin.example

import org.koin.core.annotation.Single
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.example.`interface`.MyInterface
import org.koin.ksp.generated.*

// be sure to import "import org.koin.ksp.generated.*"

@Single
class MyComponent : MyInterface



fun main() {
    startKoin {
        printLogger(Level.DEBUG)
        // else let's use our modules
        modules(
            defaultModule
        )
    }
}