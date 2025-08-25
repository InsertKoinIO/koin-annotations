package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.native.PlatformComponentA
import com.jetbrains.kmpapp.native.PlatformComponentB
import com.jetbrains.kmpapp.native.PlatformComponentD2
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.ksp.generated.*
import org.koin.mp.KoinPlatform

@KoinApplication
object KoinApp

fun initKoin(config : KoinAppDeclaration ?= null) {
    KoinApp.startKoin {
        includes(config)
        if (config == null){
            printLogger()
        }
    }

    val msgA = KoinPlatform.getKoin().get<PlatformComponentA>().sayHello()
    println("KMP App Platform: $msgA")
    val msgB = KoinPlatform.getKoin().get<PlatformComponentB>().sayHello()
    println("KMP App Platform: $msgB")
    val msgD2 = KoinPlatform.getKoin().get<PlatformComponentD2>().sayHello()
    println("KMP App Platform: $msgD2")
}
