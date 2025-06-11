package com.jetbrains.kmpapp.native

import android.content.Context
import org.koin.core.annotation.Factory
import org.koin.core.scope.Scope

@Factory
actual class PlatformComponentA actual constructor() {
    actual fun sayHello() : String = "I'm Android - A"
}

actual class PlatformComponentB actual constructor() {

    actual fun sayHello() : String = "I'm Android - B"
}

actual class PlatformComponentD actual constructor(scope: Scope) {
    
    val context : Context = scope.get<Context>()
    actual fun sayHello() : String = "I'm Android - D - with $context"
}