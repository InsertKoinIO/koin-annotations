package org.koin.sample.coffee

import org.koin.core.annotation.Single

@Single
class CoffeeMaker(private val pump: Pump, private val heater: Heater) {

    fun brew() {
        heater.on()
        pump.pump()
        println(" [_]P coffee! [_]P ")
        heater.off()
    }
}