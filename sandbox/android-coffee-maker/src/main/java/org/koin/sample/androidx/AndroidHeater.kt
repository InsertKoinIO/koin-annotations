package org.koin.sample.androidx

import android.util.Log
import org.koin.core.annotation.Single
import org.koin.example.coffee.Heater

@Single
class AndroidHeater : Heater {

    val tag = this::class.simpleName

    private var isHot = false

    override fun on() {
        isHot = true
        Log.i(tag," - heater on -")
    }

    override fun off() {
        isHot = false
        Log.i(tag," - heater off -")
    }

    override fun isHot(): Boolean {
        Log.i(tag," - heater isHot? $isHot -")
        return isHot
    }
}