package org.koin.example.test

import jakarta.inject.Named
import org.koin.core.annotation.Factory
import org.koin.example.coffee.CoffeeMaker

@Factory
@Named("test")
class CoffeeMakerTester(val coffeeMaker: CoffeeMaker)

interface CoffeeMakerTesterTest {
    fun coffeeTest()
}

class CoffeeMakerTesterTestImpl(val coffeeMaker: CoffeeMaker) : CoffeeMakerTesterTest {
    override fun coffeeTest() {
        coffeeMaker.brew()
    }
}
