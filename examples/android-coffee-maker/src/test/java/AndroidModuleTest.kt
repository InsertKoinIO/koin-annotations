package org.koin.sample.androidx

import io.ktor.client.HttpClient
import it.example.component.singleton.ExampleSingleton
import org.junit.After
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module
import org.koin.ksp.generated.startKoin
import org.koin.sample.android.library.CommonRepository
import org.koin.sample.android.library.MyScope
import org.koin.sample.androidx.app.ScopedStuff
import org.koin.sample.androidx.data.DataConsumer
import org.koin.sample.androidx.data.MyDataConsumer
import org.koin.sample.androidx.data.MyDataScope
import org.koin.sample.androidx.data.ScopeData
import org.koin.sample.androidx.di.AppModule
import org.koin.sample.androidx.di.MyKoinApp
import org.koin.sample.androidx.multi.FooB
import org.koin.sample.androidx.multi.FooC
import org.koin.sample.androidx.multi.FooD
import org.koin.sample.androidx.notcovered.IgnoredDefinition
import org.koin.sample.androidx.repository.RepositoryModule
import org.koin.sample.multi.FooA
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class AndroidModuleTest {

    @After
    fun after(){
        stopKoin()
    }

    @Test
    fun run_all_modules() {
        val koin = MyKoinApp.startKoin().koin

        val commonRepository = koin.get<CommonRepository>()
        assert(!commonRepository.lazyParam.isInitialized())
        assert(commonRepository.lazyParam != null)

        val scope = koin.createScope<MyScope>()
        scope.get<ScopedStuff>()

        assert(koin.getOrNull<DataConsumer>() != null)
        assert(koin.getOrNull<MyDataConsumer>() != null)

        assert(koin.getOrNull<ExampleSingleton>() != null)

        assert(koin.get<HttpClient>(named("clientA")) != koin.get<HttpClient>(named("clientB")))


        val defaultKoin = koinApplication {
            modules(defaultModule)
        }.koin
        assertNotNull(defaultKoin.getOrNull<IgnoredDefinition>())

        assertNotEquals(koin.get<FooB>().text,koin.get<FooA>().text)
        assertEquals(koin.get<FooB>().textBase,koin.get<FooA>().textBase)
        assertNotEquals(koin.get<FooC>().text,koin.get<FooA>().text)
        assertNotEquals(koin.get<FooD>().text,koin.get<FooA>().text)

    }

    @Test
    fun run_all_modules_common() {
        val koin = MyKoinApp.startKoin().koin

        val commonRepository = koin.get<CommonRepository>()
        assert(!commonRepository.lazyParam.isInitialized())
        assert(commonRepository.lazyParam != null)

        val scope = koin.createScope<MyScope>()
        scope.get<ScopedStuff>()

        assert(koin.getOrNull<DataConsumer>() != null)
        assert(koin.getOrNull<MyDataConsumer>() != null)

        assert(koin.getOrNull<ExampleSingleton>() != null)

        val ds = koin.createScope<MyDataScope>()
        assertNotNull(ds.getOrNull<ScopeData>())
    }

}