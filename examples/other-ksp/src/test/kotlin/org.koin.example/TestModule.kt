package org.koin.example

import org.junit.Test
import org.koin.core.Koin
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.dsl.koinApplication
import org.koin.example.animal.*
import org.koin.example.`interface`.MyInterfaceExt
import org.koin.example.newmodule.*
import org.koin.example.newmodule.ComponentWithProps.Companion.DEFAULT_ID
import org.koin.example.newmodule.mymodule.MyModule3
import org.koin.example.newmodule.mymodule.MyOtherComponent3
import org.koin.example.scope.MyScopeFactory
import org.koin.example.scope.MyScopedInstance
import org.koin.example.scope.ScopeModule
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestModule {

    @Test
    fun testApp() {
        val koin = koinApplication {
            printLogger(Level.DEBUG)
            // else let's use our modules
            modules(
                defaultModule,
                MyModule3().module,
                MyModule2().module,
                AnimalModule().module,
                ScopeModule().module
            )
        }.koin

        koin.get<MyInterfaceExt>()
        koin.get<MyOtherComponent>()
        koin.get<MyOtherComponent2>()
        koin.get<MyOtherComponent3>()
        koin.get<ComponentWithDefaultValues>()
        koin.get<MyOtherComponent3F>()

        koin.get<ComponentWithProps>().let {
            assertTrue { it.id == DEFAULT_ID }
        }
        koin.setProperty("id", "new_id")
        koin.get<ComponentWithProps>().let {
            assertTrue { it.id == "new_id" }
        }

        val animals = (1..10).map { randomGetAnimal(koin) }
        assertTrue { animals.any { it is Dog } }
        assertTrue { animals.any { it is Cat } }

        val scope = koin.createScope("my_scope_id", named("my_scope"))

        assertTrue {
            koin.get<MyScopeFactory>().msi == scope.get<MyScopedInstance>()
        }

        assertTrue {
            koin.get<MyScopeFactory>().msi == koin.get<MyScopeFactory>().msi
        }

        assertFailsWith(NoDefinitionFoundException::class) {
            koin.get<Bunny>()
        }

        assertEquals("White", koin.get<Bunny>(qualifier<WhiteBunny>()).color)

        val farm = koin.get<Farm>()
        assertEquals("White", farm.whiteBunny.color)
        assertEquals("Black", farm.blackBunny.color)
    }

    private fun randomGetAnimal(koin: Koin): Animal {
        val a = koin.get<Animal>()
        println("animal: $a")
        return a
    }
}