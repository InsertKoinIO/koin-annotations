package org.koin.example

import org.koin.core.context.GlobalContext.startKoin
import org.koin.example.components.four.MyModule4
import org.koin.example.components.one.MyModule
import org.koin.example.components.three.MyModule3
import org.koin.example.components.two.MyModule2
import org.koin.ksp.generated.module
import java.io.File

fun main() {

//    generateTestFile()

    startKoin {
        printLogger()
        modules(
//            defaultModule,
            MyModule().module,
            MyModule2().module,
            MyModule3().module,
            MyModule4().module
        )
    }
}

private fun generateTestFile(fileName : String = "output.txt") {
    val content = (1..550).map { i ->
        """
    @Single
    class ComponentAAAA$i
    @Single
    class ComponentBBBBB$i(val a : ComponentAAAA$i)
    @Single
    class ComponentCCCCC$i(val a : ComponentAAAA$i, val b : ComponentBBBBB$i)
        """.trimIndent()
    } .joinToString(separator = "\n")

    val f = File(fileName)
        f.createNewFile()
    f.writeText(content)
}