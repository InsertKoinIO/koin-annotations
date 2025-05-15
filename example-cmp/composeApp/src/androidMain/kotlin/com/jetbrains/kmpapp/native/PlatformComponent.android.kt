package com.jetbrains.kmpapp.native

import org.koin.core.annotation.Factory

@Factory
actual class PlatformComponentA actual constructor() {
    actual fun sayHello() : String = "I'm Android - A"
}

actual class PlatformComponentB actual constructor() {

    actual fun sayHello() : String = "I'm Android - B"
}