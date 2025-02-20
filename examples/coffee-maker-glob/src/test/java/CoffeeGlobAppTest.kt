import org.junit.Test
import org.koin.compiler.util.anyMatch
import org.koin.compiler.util.matchesGlob
import org.koin.compiler.util.toGlobRegex
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
import org.koin.example.di.CoffeeAppAndTesterModule
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.measureTime

class CoffeeGlobAppTest {

    @Test
    fun all_coffee_test() {
        startKoin {
            printLogger(Level.DEBUG)
            // if no module
//        defaultModule()

            // else let's use our modules
            modules(
                CoffeeAppAndTesterModule().module,
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
        koin.setProperty("prop_id", id)
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

        koin.createScope("_ID2_", named(MY_SCOPE_SESSION))
            .get<MyScopedSessionComponent>()

        assert(koin.get<TestComponentConsumer3>{ parametersOf(null) }.id == null)
        assert(koin.get<TestComponentConsumer3> { parametersOf("42") }.id == "42")

        koin.get<IncludedComponent>()

        assert(koin.get<CoffeePumpList>().list.size == 2)
        assert(koin.get<PumpCounter>().count == 2)

        assert(koin.getOrNull<MyDetachCoffeeComponent>() != null)

        stopKoin()
    }


    /// -- Internal glob tests --
    @Test
    fun `toGlobRegex converts single star correctly`() {
        val glob = "com.example.*"
        val regex = glob.toGlobRegex()
        // "com.example" doesn't contain any star, so ensureGlobPattern appends ".*",
        // but the glob already has a star so it will be replaced as [^.]*.
        // Expected regex: ^com\.example\.[^.]*$
        assertTrue(regex.matches("com.example.foo"))
        assertFalse(regex.matches("com.examplefoo"))
    }

    @Test
    fun `toGlobRegex converts double star correctly`() {
        val glob = "com.**.service"
        val regex = glob.toGlobRegex()
        // The "**" should become ".*", allowing for multiple levels in between.
        assertTrue(regex.matches("com.foo.service"))
        assertTrue(regex.matches("com.foo.bar.service"))
        assertFalse(regex.matches("com.service"))
    }

    @Test
    fun `toGlobRegex converts double star correctly - no leading dot`() {
        val glob = "com**.service"
        val regex = glob.toGlobRegex()
        // The "**" should become ".*", allowing for multiple levels in between.
        assertTrue(regex.matches("com.foo.service"))
        assertTrue(regex.matches("com.foo.bar.service"))
        assertTrue(regex.matches("com.service"))
    }

    @Test
    fun `ensureGlobPattern appends recursive glob when no star is present`() {
        val glob = "com.example"
        val regex = glob.toGlobRegex()
        // Since "com.example" doesn't contain a star, ensureGlobPattern appends ".*",
        // resulting in a regex that matches any string starting with "com.example".
        assertTrue(regex.matches("com.example.foo"))
        // This may also match "com.exampleFoo" because no dot separator is enforced.
        assertTrue(regex.matches("com.examplefoo"))
    }

    @Test
    fun `matchesGlob performs an exact match`() {
        assertTrue("com.example.foo".matchesGlob("com.example.foo"))
        assertFalse("com.example.foo".matchesGlob("com.example.bar"))
    }

    @Test
    fun `matchesGlob supports ignoreCase flag`() {
        assertTrue("com.example.foo".matchesGlob("com.example.Foo", ignoreCase = true))
        assertFalse("com.example.foo".matchesGlob("com.example.Foo", ignoreCase = false))
    }

    @Test
    fun `anyMatch finds a matching key in a map`() {
        val testMap = mapOf(
            "com.example" to 0,
            "com.example.foo" to 1,
            "com.test.bar" to 2,
            "org.sample.baz" to 3
        )
        assertTrue(testMap.anyMatch("com.example*.*"))
        assertTrue(testMap.anyMatch("com.**"))
        assertTrue(testMap.anyMatch("co*.ex**"))
        assertTrue(testMap.anyMatch("co*.ex*.**"))
        assertTrue(testMap.anyMatch("co*.ex*"))
        assertFalse(testMap.anyMatch("org.example*"))
    }

    @Test
    fun `test conversion of various glob patterns to regex`() {
        val singleStarGlob = "com.exampl*"
        val mixedGlob = "com.exampl*.**"
        val doubleStarGlob = "com.exampl**"

        assertTrue(singleStarGlob.toGlobRegex().matches("com.example"))
        assertFalse(singleStarGlob.toGlobRegex().matches("com.example.foo"))

        assertFalse(mixedGlob.toGlobRegex().matches("com.example"))
        assertTrue(mixedGlob.toGlobRegex().matches("com.example.foo"))

        assertTrue(doubleStarGlob.toGlobRegex().matches("com.example"))
        assertTrue(doubleStarGlob.toGlobRegex().matches("com.example.foo"))
    }

}