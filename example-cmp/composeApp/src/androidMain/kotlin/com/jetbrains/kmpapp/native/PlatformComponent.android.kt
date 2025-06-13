package com.jetbrains.kmpapp.native

import com.jetbrains.kmpapp.di.ContextWrapper
import org.koin.core.annotation.Factory

@Factory
actual class PlatformComponentA {
    actual fun sayHello() : String = "I'm Android - A"
}

actual class PlatformComponentB {

    actual fun sayHello() : String = "I'm Android - B"
}

class PlatformComponentDAndroid(val ctx : ContextWrapper) : PlatformComponentD{
    override fun sayHello() : String = "I'm Android - D - with ${ctx.context}"
}

@Factory
actual class PlatformComponentD2 actual constructor(val ctx : ContextWrapper) {
    actual fun sayHello() : String = "I'm Android - D2 - ${ctx.context}"
}