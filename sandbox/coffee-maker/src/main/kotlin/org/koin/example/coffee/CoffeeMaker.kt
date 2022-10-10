package org.koin.example.coffee

import org.koin.core.annotation.LazyParam
import org.koin.core.annotation.Singleton
import org.koin.example.coffee.pump.Pump
import org.koin.example.service.Cup

@Singleton
class CoffeeMaker(private val pump: Pump, private val heater: Heater, @LazyParam("CoffeeMug") private val cup: Lazy<Cup>) {

    fun brew() {
        heater.on()
        pump.pump()
        println(" [_]P coffee! [_]P ")
        heater.off()
        cup.value.apply {
            println("↓ ↓ filling ↓ ↓")
            fill()
        }
    }
}