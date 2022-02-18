package org.koin.sample.coffee

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
// generated
import org.koin.ksp.generated.*
import org.koin.sample.coffee.component.CoffeeMaker
import org.koin.sample.coffee.di.CoffeeAppModule

class CoffeeApplication : KoinComponent {
    private val maker: CoffeeMaker by inject()

    fun run(){
        maker.brew()
    }
}

fun main(){
    startKoin {
        modules(CoffeeAppModule().module)
    }
    CoffeeApplication().run()
}