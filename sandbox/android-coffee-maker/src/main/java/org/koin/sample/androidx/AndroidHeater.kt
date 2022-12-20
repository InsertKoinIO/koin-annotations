package org.koin.sample.androidx

import android.util.Log
import org.koin.core.annotation.Single

@Single
class AndroidHeater {

    val tag = this::class.simpleName

    private var isHot = false

    fun on() {
        isHot = true
        Log.i(tag, " - heater on -")
    }

    fun off() {
        isHot = false
        Log.i(tag, " - heater off -")
    }

    fun isHot(): Boolean {
        Log.i(tag, " - heater isHot? $isHot -")
        return isHot
    }
}