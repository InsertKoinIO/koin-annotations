package org.koin.sample.coffee

interface Heater {
    fun on()
    fun off()
    fun isHot(): Boolean
}