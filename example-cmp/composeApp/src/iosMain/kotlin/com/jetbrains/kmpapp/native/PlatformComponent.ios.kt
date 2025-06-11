package com.jetbrains.kmpapp.native

import org.koin.core.annotation.Factory
import org.koin.core.scope.Scope

@Factory
actual class PlatformComponentA actual constructor() {
    actual fun sayHello() : String = "I'm iOS - A"
}

actual class PlatformComponentB actual constructor() {
    actual fun sayHello() : String = "I'm iOS - B"
}

actual class PlatformComponentD actual constructor(scope: Scope) {
    actual fun sayHello() : String = "I'm iOS - D"
}