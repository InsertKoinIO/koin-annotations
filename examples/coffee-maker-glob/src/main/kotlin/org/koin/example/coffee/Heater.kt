package org.koin.example.coffee

interface Heater {
    fun on()
    fun off()
    fun isHot(): Boolean
}