package org.koin.example

import org.koin.core.annotation.KoinApplication
import org.koin.ksp.generated.startKoin

@KoinApplication
object AllCompApplication

fun main() {

    AllCompApplication.startKoin {
        printLogger()
    }

}

//private fun generateTestFile(fileName : String = "output.txt") {
//    val content = (1..550).map { i ->
//        """
//    @Single
//    class ComponentAAAA$i
//    @Single
//    class ComponentBBBBB$i(val a : ComponentAAAA$i)
//    @Single
//    class ComponentCCCCC$i(val a : ComponentAAAA$i, val b : ComponentBBBBB$i)
//        """.trimIndent()
//    } .joinToString(separator = "\n")
//
//    val f = File(fileName)
//        f.createNewFile()
//    f.writeText(content)
//}