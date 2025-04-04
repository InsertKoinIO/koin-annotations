import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.example.CoffeeApp
import org.koin.example.coffee.CoffeePumpList
import org.koin.example.coffee.MyDetachCoffeeComponent
import org.koin.example.coffee.pump.PumpCounter
import org.koin.example.di.CoffeeAppModule
import org.koin.example.di.CoffeeTesterModule
import org.koin.example.tea.TeaModule
import org.koin.example.tea.TeaPot
import org.koin.example.test.CoffeeMakerTester
import org.koin.example.test.CoffeeMakerTesterTest
import org.koin.example.test.ext.*
import org.koin.example.test.ext2.ExternalModule
import org.koin.example.test.include.IncludedComponent
import org.koin.example.test.scope.*
import org.koin.ksp.generated.module
import org.koin.mp.KoinPlatformTools
import kotlin.test.*
import kotlin.time.measureTime

class CoffeeAppTest {

    @Test
    fun all_coffee_test(){
        startKoin {
            printLogger(Level.DEBUG)
            // if no module
//        defaultModule()

            // else let's use our modules
            modules(
                CoffeeAppModule().module,
                CoffeeTesterModule().module,
                TeaModule().module,
                org.koin.example.test.ext.ExternalModule().module,
                ExternalModule().module,
                ScopeModule().module
            )
        }

        val coffeeShop = CoffeeApp()
        val time = measureTime {
            coffeeShop.maker.brew()
        }
        println("Got Coffee in $time")

        // Tests
        val koin = KoinPlatformTools.defaultContext().get()
        koin.get<TeaPot>().heater
        koin.get<CoffeeMakerTester>(StringQualifier("test"))
        koin.get<CoffeeMakerTesterTest>().coffeeTest()
        koin.get<TestComponent>(StringQualifier("tc"))
        val id = "id"
        assert(koin.get<TestComponentConsumer> { parametersOf(id) }.id == id)
        assert(koin.get<TestComponentConsumer2> { parametersOf(id) }.id == id)
        koin.setProperty("prop_id",id)
        assert(koin.get<PropertyComponent>().id == id)
        assert(koin.get<PropertyComponent2>().id == id)

        val myScope = MyScope()
        val scopeS = koin.createScope("_ID1_", named<MyScope>(), myScope)
        assert(myScope == scopeS.get<MyScopedComponent>().myScope)
        assert(myScope == scopeS.get<MyScopedComponent2>().myScope)
        assert(myScope == scopeS.get<MyScopedComponent3>().myScope)
        assert(scopeS.get<MyScopedComponent3>() != scopeS.get<MyScopedComponent3>())
        assert(myScope == scopeS.get<MyScopedComponent4>().myScope)

        assert(scopeS.getOrNull<AdditionalTypeScope2>() != null)

        val myScopeSession = koin.createScope("_ID2_", named(MY_SCOPE_SESSION))
        assertNotNull(myScopeSession.getOrNull<MyScopedSessionComponent>())

        // BEGIN MUltiScope
        val myAnotherScope = MyAnotherScope()
        val anotherScopeS = koin.createScope("_ID3_", named<MyAnotherScope>(), myAnotherScope)
        assertEquals(myScope, scopeS.get<MyScopedComponentMultiScope>().myScope.value)
        assertFails { scopeS.get<MyScopedComponentMultiScope>().myAnotherScope.value }
        assertEquals(myAnotherScope, anotherScopeS.get<MyScopedComponentMultiScope>().myAnotherScope.value)
        assertFails { anotherScopeS.get<MyScopedComponentMultiScope>().myScope.value }

        assertEquals(myScope, scopeS.get<MyScopedComponentMultiScope2>().myScope.value)
        assertFails { scopeS.get<MyScopedComponentMultiScope2>().myAnotherScope.value }
        assertEquals(myAnotherScope, anotherScopeS.get<MyScopedComponentMultiScope2>().myAnotherScope.value)
        assertFails { anotherScopeS.get<MyScopedComponentMultiScope2>().myScope.value }

        assertEquals(myScope, scopeS.get<MyScopedComponentMultiScope3>().myScope.value)
        assertFails { scopeS.get<MyScopedComponentMultiScope3>().myAnotherScope.value }
        assertEquals(myAnotherScope, anotherScopeS.get<MyScopedComponentMultiScope3>().myAnotherScope.value)
        assertFails { anotherScopeS.get<MyScopedComponentMultiScope3>().myScope.value }
        assertNotEquals(scopeS.get<MyScopedComponentMultiScope3>(), scopeS.get<MyScopedComponentMultiScope3>())
        assertNotEquals(anotherScopeS.get<MyScopedComponentMultiScope3>(), anotherScopeS.get<MyScopedComponentMultiScope3>())

        assertEquals(myScope, scopeS.get<MyScopedComponentMultiScope4>().myScope.value)
        assertFails { scopeS.get<MyScopedComponentMultiScope4>().myAnotherScope.value }
        assertEquals(myAnotherScope, anotherScopeS.get<MyScopedComponentMultiScope4>().myAnotherScope.value)
        assertFails { anotherScopeS.get<MyScopedComponentMultiScope4>().myScope.value }
        assertNotEquals(scopeS.get<MyScopedComponentMultiScope4>(), scopeS.get<MyScopedComponentMultiScope4>())
        assertNotEquals(anotherScopeS.get<MyScopedComponentMultiScope4>(), anotherScopeS.get<MyScopedComponentMultiScope4>())

        assertNotNull(scopeS.getOrNull<AdditionalTypeMultiScope>())
        assertNotNull(anotherScopeS.getOrNull<AdditionalTypeMultiScope>())

        assertNotNull(scopeS.getOrNull<AdditionalTypeMultiScope2>())
        assertSame(scopeS.getOrNull<AdditionalTypeMultiScope2>(), scopeS.getOrNull<MyScopedComponentMultiScope5>())
        assertNotNull(anotherScopeS.getOrNull<AdditionalTypeMultiScope2>())
        assertSame(anotherScopeS.getOrNull<AdditionalTypeMultiScope2>(), anotherScopeS.getOrNull<MyScopedComponentMultiScope5>())

        assertNotNull(myScopeSession.getOrNull<MyScopedSessionComponentMultiScope>())
        val myScopeSession2 = koin.createScope("_ID4_", named(MY_SCOPE_SESSION2))
        assertNotNull(myScopeSession2.getOrNull<MyScopedSessionComponentMultiScope>())
        // END MUltiScope

        assert(koin.get<TestComponentConsumer3>{ parametersOf(null) }.id == null)
        assert(koin.get<TestComponentConsumer3>{ parametersOf("42") }.id == "42")

        koin.get<IncludedComponent>()

        assert(koin.get<CoffeePumpList>().list.size == 2)
        assert(koin.get<PumpCounter>().count == 2)

        assert(koin.getOrNull<MyDetachCoffeeComponent>() != null)

        stopKoin()
    }
}