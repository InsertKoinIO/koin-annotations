package org.koin.sample.coffee.component

import org.koin.core.annotation.Single

@Single
class SugarDispenser : Dispenser {

    override fun dispense() {
        println("Adding sugar to coffee")
    }
}