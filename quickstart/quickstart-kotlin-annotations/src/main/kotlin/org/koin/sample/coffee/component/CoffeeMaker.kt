package org.koin.sample.coffee.component

import org.koin.core.annotation.GetAll
import org.koin.core.annotation.Single

@Single
class CoffeeMaker(private val pump: Pump, private val heater: Heater, @GetAll private val dispensers: List<Dispenser>) {

    fun brew() {
        heater.on()
        pump.pump()
        dispensers.forEach(Dispenser::dispense)
        println(" [_]P coffee! [_]P ")
        heater.off()
    }
}