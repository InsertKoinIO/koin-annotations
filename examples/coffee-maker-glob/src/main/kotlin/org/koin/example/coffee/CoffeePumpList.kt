package org.koin.example.coffee

import org.koin.core.annotation.Single
import org.koin.example.coffee.pump.Pump

@Single
class CoffeePumpList(val list : List<Pump>)