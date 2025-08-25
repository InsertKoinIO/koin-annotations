package org.koin.example.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.coffee.CoffeeMaker
import org.koin.example.coffee.pump.Pump
import org.koin.example.coffee.pump.PumpCounter
import org.koin.example.test.CoffeeMakerTesterTest
import org.koin.example.test.CoffeeMakerTesterTestImpl

/**
 * CoffeeAppAndTesterModule
 *
 * This module is responsible for configuring component scanning for both the Coffee (glob) application
 * and its associated tester components.
 *
 * It scans the following package patterns:
 *   - "org.koin.example.coff*.**"
 *   - "org.koin.example.coff*"
 *   - "org.koin.example.tes*"
 *
 * Note: The two patterns for the coffee components ("org.koin.example.coff*.**" and "org.koin.example.coff*")
 * can be consolidated into a single, more concise pattern "org.koin.example.coff**".
 */
@Module
@Configuration
@ComponentScan("org.koin.example.coff*.**", "org.koin.example.coff*", "org.koin.example.tes*")
class CoffeeAppAndTesterModule {

    @Single
    fun pumpCounter(list: List<Pump>) = PumpCounter(list)


    @Single
    fun CoffeeMakerTesterTest(coffeeMaker: CoffeeMaker): CoffeeMakerTesterTest = CoffeeMakerTesterTestImpl(coffeeMaker)
}
