package org.koin.sample.coffee.component

interface Heater {
    fun on()
    fun off()
    fun isHot(): Boolean
}