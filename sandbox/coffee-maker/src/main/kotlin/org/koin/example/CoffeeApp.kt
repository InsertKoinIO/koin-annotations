package org.koin.example

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.core.time.measureDuration
import org.koin.example.coffee.CoffeeMaker
import org.koin.example.di.CoffeeAppModule
import org.koin.example.di.CoffeeTesterModule
import org.koin.example.tea.TeaModule
import org.koin.example.tea.TeaPot
import org.koin.example.test.*
import org.koin.example.test.ext.*
import org.koin.example.test.include.IncludedComponent
import org.koin.example.test.scope.*
import org.koin.ksp.generated.*
import org.koin.mp.KoinPlatformTools

class CoffeeApp : KoinComponent {
    val maker: CoffeeMaker by inject()
}

// be sure to import "import org.koin.ksp.generated.*"

fun main() {
    startKoin {
        printLogger(Level.DEBUG)
        // if no module
//        defaultModule()

        // else let's use our modules
        modules(
            CoffeeAppModule().module,
            CoffeeTesterModule().module,
            TeaModule().module,
            ExternalModule().module,
            org.koin.example.test.ext2.ExternalModule().module,
            ScopeModule().module
        )
    }

    val coffeeShop = CoffeeApp()
    measureDuration("Got Coffee") {
        coffeeShop.maker.brew()
    }
}

fun measureDuration(msg: String, code: () -> Unit): Double {
    val duration = measureDuration(code)
    println("$msg in $duration ms")
    return duration
}