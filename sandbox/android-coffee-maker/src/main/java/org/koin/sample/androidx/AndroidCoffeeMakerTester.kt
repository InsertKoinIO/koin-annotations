package org.koin.sample.androidx

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import org.koin.example.test.CoffeeMakerTester

@Factory
class AndroidCoffeeMakerTester(@Named(name = "test") val coffeeMakerTester : CoffeeMakerTester)