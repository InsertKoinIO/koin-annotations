package org.koin.example

import org.koin.core.context.startKoin
import org.koin.core.time.measureDuration
import org.koin.example.components.one.MyModule
import org.koin.example.components.three.MyModule3
import org.koin.example.components.two.MyModule2

import org.koin.ksp.generated.*

fun main() {
//    generate()

    startKoin {
        printLogger()
        modules(
//            defaultModule,
            MyModule().module,
            MyModule2().module,
            MyModule3().module
        )
    }
}

private fun generate() {
    for (i in 1..100) {
        println(
            """
    @Single
    class ComponentA$i
    @Single
    class ComponentB$i(val a : ComponentA$i)
    @Single
    class ComponentC$i(val a : ComponentA$i, val b : ComponentB$i)
        """.trimIndent()
        )
    }
}