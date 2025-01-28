package org.koin.example.coffee.pump

class PumpCounter(val pump : List<Pump>){
    val count = pump.size
}