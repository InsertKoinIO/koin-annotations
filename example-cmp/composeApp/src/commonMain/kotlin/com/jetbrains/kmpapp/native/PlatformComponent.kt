package com.jetbrains.kmpapp.native

import org.koin.core.annotation.Factory
import org.koin.core.scope.Scope

@Factory
expect class PlatformComponentA() {
    fun sayHello() : String
}

expect class PlatformComponentB() {
    fun sayHello() : String
}

expect class PlatformComponentD(scope: Scope) {
    fun sayHello() : String
}