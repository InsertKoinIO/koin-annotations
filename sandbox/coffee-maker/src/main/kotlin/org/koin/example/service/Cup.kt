package org.koin.example.service


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
