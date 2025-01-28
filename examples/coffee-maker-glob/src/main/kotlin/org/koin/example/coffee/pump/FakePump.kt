package org.koin.example.coffee.pump

import org.koin.core.annotation.Single

@Single
class FakePump : Pump {
    override fun pump() {
        println("fake pump")
    }
}