package org.koin.example.coffee.pump

import org.koin.core.annotation.Single
import org.koin.example.coffee.Heater

@Single
class Thermosiphon(private val heater: Heater) : Pump {
    override fun pump() {
        if (heater.isHot()) {
            println("=> => pumping => =>")
        }
    }
}