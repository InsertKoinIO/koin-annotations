package org.koin.example.service

/**
 * Base cup class to be filled
 *
 * This class prints "<child class name> is ready to be filled" when initialized.
 * Purpose is showing the class is not created when injected lazily
 *
 * @author Alperen Babagil
 */

abstract class Cup() {

    private val name by lazy { setMyName() }

    abstract fun setMyName(): String

    init {
        println("$name is ready to be filled")
    }

    fun fill(){
        println("$name is filled")
    }
}
