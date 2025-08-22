package org.koin.example

import org.koin.core.annotation.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.logger.Level
import org.koin.example.coffee.CoffeeMaker
import org.koin.ksp.generated.*
import kotlin.time.measureTime

class CoffeeApp : KoinComponent {
    val maker: CoffeeMaker by inject()
}

// be sure to import "import org.koin.ksp.generated.*"

@KoinApplication
object CoffeeGlobApp

fun main() {

    CoffeeGlobApp.startKoin {
        printLogger(Level.DEBUG)
    }

//    startKoin {
//        printLogger(Level.DEBUG)
//        // if no module
////        defaultModule()
//
//        // else let's use our modules
//        modules(
//            CoffeeAppAndTesterModule().module,
//            TeaModule().module,
//            ExternalModule().module,
//            org.koin.example.test.ext2.ExternalModule().module,
//            ScopeModule().module
//        )
//    }

    val coffeeShop = CoffeeApp()
    val t = measureTime {
        coffeeShop.maker.brew()
    }
    println("Got Coffee in $t")
}