package org.koin.example.service

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single(binds = [Cup::class])
@Named("CoffeeMug")

class CoffeeMug : Cup() {

    override fun setMyName(): String = "CoffeeMug"

}