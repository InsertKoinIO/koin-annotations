package org.koin.sample.coffee

import org.koin.core.annotation.Single

@Single
class Thermosiphon(private val heater: Heater) : Pump {
    override fun pump() {
        if (heater.isHot()) {
            println("=> => pumping => =>")
        }
    }
}