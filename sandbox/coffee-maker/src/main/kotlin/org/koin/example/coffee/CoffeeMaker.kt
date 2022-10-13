package org.koin.example.coffee

import org.koin.core.annotation.LazyParam
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import org.koin.example.coffee.pump.Pump
import org.koin.example.service.*

@Singleton
class CoffeeMaker(
    private val pump: Pump,
    private val heater: Heater,
    @LazyParam(name = "CoffeeMugStringQualifierLazy") private val coffeeMugStringQualifierLazy: Lazy<Cup>,
    @LazyParam(value = CoffeeMugTypeQualifierLazy::class) private val coffeeMugTypeQualifierLazy: Lazy<Cup>,
    @LazyParam private val coffeeMugNoQualifierLazy: Lazy<Cup>,
    @Named(name = "CoffeeMugStringQualifier") private val coffeeMugStringQualifier: Cup,
    @Named(value = CoffeeMugTypeQualifier::class) private val coffeeMugTypeQualifier: Cup,

) {

    fun brew() {
        heater.on()
        pump.pump()
        println(" [_]P coffee! [_]P ")
        heater.off()
        println("↓ ↓ filling ↓ ↓")
        mutableListOf(
            coffeeMugStringQualifierLazy,
            coffeeMugTypeQualifierLazy,
            coffeeMugNoQualifierLazy,
            coffeeMugStringQualifier,
            coffeeMugTypeQualifier
        ).apply {
            shuffle()
        }.forEach {
            val cup = when {
                (it is Lazy<*>) -> {
                    (it.value)
                }
                else -> it
            } as Cup
            cup.fill()
        }
    }
}