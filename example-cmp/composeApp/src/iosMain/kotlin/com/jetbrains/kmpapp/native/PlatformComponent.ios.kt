package com.jetbrains.kmpapp.native

import com.jetbrains.kmpapp.di.ContextWrapper
import org.koin.core.annotation.Factory

@Factory
actual class PlatformComponentA {
    actual fun sayHello() : String = "I'm iOS - A"
}

actual class PlatformComponentB {
    actual fun sayHello() : String = "I'm iOS - B"
}

class PlatformComponentDiOS : PlatformComponentD{
    override fun sayHello() : String = "I'm iOS - D"
}

@Factory
actual class PlatformComponentD2 actual constructor(val ctx : ContextWrapper) {
    actual fun sayHello() : String = "I'm iOS - D22 - $ctx"
}