package com.jetbrains.kmpapp.native

import org.koin.core.annotation.Factory

@Factory
expect class PlatformComponentA() {
    fun sayHello() : String
}

expect class PlatformComponentB() {
    fun sayHello() : String
}