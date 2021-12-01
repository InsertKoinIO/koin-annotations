package org.koin.example.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.coffee.CoffeeMaker
import org.koin.example.test.CoffeeMakerTesterTest
import org.koin.example.test.CoffeeMakerTesterTestImpl

@Module
@ComponentScan("org.koin.example.test")
class CoffeeTesterModule {

    @Single
    fun CoffeeMakerTesterTest(coffeeMaker: CoffeeMaker) : CoffeeMakerTesterTest = CoffeeMakerTesterTestImpl(coffeeMaker)
}