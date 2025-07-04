package org.koin.example

import org.junit.Test
import org.koin.core.Koin
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.dsl.koinApplication
import org.koin.example.animal.*
import org.koin.example.binds.BindTestsModule
import org.koin.example.binds.ClientWithBinds
import org.koin.example.by.example.ByExampleSingle
import org.koin.example.by.example.ByModule
import org.koin.example.defaultparam.COMPONENT_DEFAULT
import org.koin.example.defaultparam.Component
import org.koin.example.defaultparam.MyModule
import org.koin.example.injparam.InjectedParamModule
import org.koin.example.injparam.MyInjectFactory
import org.koin.example.`interface`.MyInterfaceExt
import org.koin.example.newmodule.*
import org.koin.example.newmodule.ComponentWithProps.Companion.DEFAULT_ID
import org.koin.example.newmodule.mymodule.MyModule3
import org.koin.example.newmodule.mymodule.MyOtherComponent3
import org.koin.example.qualifier.LazyStuffCounter
import org.koin.example.qualifier.QualifierModule
import org.koin.example.qualifier.StuffCounter
import org.koin.example.qualifier.StuffList
import org.koin.example.scope.MyScopeFactory
import org.koin.example.scope.MyScopedInstance
import org.koin.example.scope.ScopeModule
import org.koin.example.supertype.A
import org.koin.example.supertype.B
import org.koin.example.supertype.C
import org.koin.example.supertype.D
import org.koin.example.supertype.SuperTypesModule
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
                ScopeModule().module,
                ByModule().module,
                MyModule().module,
                SuperTypesModule().module,
                BindTestsModule().module,
                QualifierModule().module,
                InjectedParamModule().module,
                QualifierModule().module,
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
            scope.get<MyScopeFactory>().msi == scope.get<MyScopedInstance>()
        }

        assertTrue {
            scope.get<MyScopeFactory>().msi == scope.get<MyScopeFactory>().msi
        }

        assertFailsWith(NoDefinitionFoundException::class) {
            koin.get<Bunny>()
        }

        assertEquals("White", koin.get<Bunny>(qualifier<WhiteBunny>()).color)

        val farm = koin.get<Farm>()
        assertEquals("White", farm.whiteBunny.color)
        assertEquals("Black", farm.blackBunny.color)

        assertNotNull(koin.getOrNull<ByExampleSingle>())

        assertEquals(COMPONENT_DEFAULT,koin.get<Component>().param) // display warning in build logs

        assertNotNull(koin.getOrNull<C>())
        assertEquals(koin.get<C>(),koin.get<B>())
        assertEquals(koin.get<C>(),koin.get<D>())
        assertNull(koin.getOrNull<A>())

        assertNotNull(koin.getOrNull<ClientWithBinds>())

        assertEquals(2,koin.get<StuffList>(named("stuffs")).list.size)
        assertEquals(2,koin.get<StuffCounter>().list.size)

        assertEquals(2,koin.get<LazyStuffCounter>().lazyCounter.value.list.size)
        assertEquals("lazy",koin.get<LazyStuffCounter>().lazyCounter.value.name)

        val ints = listOf(1,2,3)
        assertEquals(ints,koin.get<MyInjectFactory>{ parametersOf(ints) }.ints)

        assertEquals(2,koin.get<StuffList>(named("another-counter")).list.size)
        assertEquals("another-counter",koin.get<StuffCounter>().name)
    }



    private fun randomGetAnimal(koin: Koin): Animal {
        val a = koin.get<Animal>()
        println("animal: $a")
        return a
    }
}