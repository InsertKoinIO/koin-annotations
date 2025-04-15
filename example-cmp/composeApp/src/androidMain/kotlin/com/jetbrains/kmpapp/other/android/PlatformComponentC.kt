package com.jetbrains.kmpapp.other.android

import android.content.Context
import org.koin.core.annotation.Factory

@Factory
class PlatformComponentC(val context: Context) {
    fun sayHello() : String = "I'm Android - C - $context"
}