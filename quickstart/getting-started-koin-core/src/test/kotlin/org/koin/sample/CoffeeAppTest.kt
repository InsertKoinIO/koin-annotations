package org.koin.sample

import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.ksp.generated.*
import org.koin.sample.coffee.CoffeeAppModule
import org.koin.sample.coffee.CoffeeMaker
import org.koin.sample.coffee.Heater
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class CoffeeAppTest : KoinTest {

    private val coffeeMaker: CoffeeMaker by inject()
    private val heater: Heater by inject()

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mock(clazz.java)
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger(Level.DEBUG)
        modules(CoffeeAppModule().module)
    }

    @Test
    fun testHeaterIsTurnedOnAndThenOff() {
        declareMock<Heater> {
            given(isHot()).willReturn(true)
        }

        coffeeMaker.brew()

        verify(heater).on()
        verify(heater).isHot()
        verify(heater).off()
    }
}