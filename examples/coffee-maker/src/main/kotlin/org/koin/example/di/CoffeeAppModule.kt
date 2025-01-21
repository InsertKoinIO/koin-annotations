package org.koin.example.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.coffee.CoffeeMaker
import org.koin.example.coffee.pump.Pump
import org.koin.example.coffee.pump.PumpCounter
import org.koin.example.test.CoffeeMakerTesterTest
import org.koin.example.test.CoffeeMakerTesterTestImpl

@Module
@ComponentScan(values = ["org.koin.example.coffee"])
class CoffeeAppModule {

    @Single
    fun pumpCounter(list : List<Pump>) = PumpCounter(list)
}

@Module
@ComponentScan(values = ["org.koin.example.test"])
class CoffeeTesterModule {

    @Single
    fun CoffeeMakerTesterTest(coffeeMaker: CoffeeMaker) : CoffeeMakerTesterTest = CoffeeMakerTesterTestImpl(coffeeMaker)
}
