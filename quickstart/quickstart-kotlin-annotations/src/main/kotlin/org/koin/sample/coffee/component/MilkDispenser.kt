package org.koin.sample.coffee.component

import org.koin.core.annotation.Single

@Single
class MilkDispenser : Dispenser {

    override fun dispense() {
        println("Adding milk to coffee")
    }
}