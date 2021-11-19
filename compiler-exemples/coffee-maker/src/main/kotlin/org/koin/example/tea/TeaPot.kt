package org.koin.example.tea

import org.koin.core.annotation.Single
import org.koin.example.coffee.Heater

@Single
class TeaPot(val heater: Heater)