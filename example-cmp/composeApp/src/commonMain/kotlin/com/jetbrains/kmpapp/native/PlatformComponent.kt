package com.jetbrains.kmpapp.native

import com.jetbrains.kmpapp.di.ContextWrapper
import org.koin.core.annotation.Factory
import org.koin.core.scope.Scope

@Factory
expect class PlatformComponentA() {
    fun sayHello() : String
}

expect class PlatformComponentB() {
    fun sayHello() : String
}

interface PlatformComponentD {
    fun sayHello() : String
}

@Factory
expect class PlatformComponentD2(ctx : ContextWrapper) {
    fun sayHello() : String
}