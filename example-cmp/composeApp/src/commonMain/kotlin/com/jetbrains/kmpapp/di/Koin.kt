package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.native.PlatformComponentA
import com.jetbrains.kmpapp.native.PlatformComponentB
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.ksp.generated.*
import org.koin.mp.KoinPlatform

fun initKoin(config : KoinAppDeclaration ?= null) {
    startKoin {
        printLogger()
        includes(config)
        modules(
            AppModule().module,
        )
    }

    val msgA = KoinPlatform.getKoin().get<PlatformComponentA>().sayHello()
    println("KMP App Platform: $msgA")

    val msgB = KoinPlatform.getKoin().get<PlatformComponentB>().sayHello()
    println("KMP App Platform: $msgB")
}
