package org.koin.example.coffee

import org.koin.core.annotation.Single

@Single
class ElectricHeater : Heater {

    private var heating: Boolean = false

    override fun on() {
        println("~ ~ ~ heating ~ ~ ~")
        heating = true
    }

    override fun off() {
        heating = false
    }

    override fun isHot(): Boolean = heating
}