package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.native.PlatformComponentA
import com.jetbrains.kmpapp.native.PlatformComponentB
import com.jetbrains.kmpapp.native.PlatformComponentD2
import io.kotzilla.sdk.analytics.koin.analytics
import io.kotzilla.sdk.config.Environment
import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import com.jetbrains.generated.*
//import org.koin.ksp.generated.*
import org.koin.mp.KoinPlatform

@KoinApplication
object KoinApp

fun initKoin(config : KoinAppDeclaration ?= null) {
    KoinApp.startKoin {
        includes(config)

        // Activate App analyses & Perf tracing
        // Check kotzilla.json file
        analytics()
    }

    val msgA = KoinPlatform.getKoin().get<PlatformComponentA>().sayHello()
    println("KMP App Platform: $msgA")
    val msgB = KoinPlatform.getKoin().get<PlatformComponentB>().sayHello()
    println("KMP App Platform: $msgB")
    val msgD2 = KoinPlatform.getKoin().get<PlatformComponentD2>().sayHello()
    println("KMP App Platform: $msgD2")
}
