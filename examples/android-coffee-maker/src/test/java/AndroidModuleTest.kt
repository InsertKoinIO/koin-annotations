package org.koin.sample.androidx

import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.ksp.generated.module
import org.koin.sample.android.library.CommonRepository
import org.koin.sample.android.library.MyScope
import org.koin.sample.androidx.app.MyPresenter
import org.koin.sample.androidx.app.ScopedStuff
import org.koin.sample.androidx.data.DataConsumer
import org.koin.sample.androidx.data.MyDataConsumer
import org.koin.sample.androidx.di.AppModule
import org.koin.sample.androidx.di.DataModule
import org.koin.sample.androidx.repository.RepositoryModule

class AndroidModuleTest {

    @Test
    fun run_all_modules() {
        val koin = startKoin {
            modules(
//                defaultModule,
                DataModule().module,
                RepositoryModule().module,
                AppModule().module,
                RootModule().module
            )
        }.koin

        val commonRepository = koin.get<CommonRepository>()
        assert(!commonRepository.lazyParam.isInitialized())
        assert(commonRepository.lazyParam != null)

        val scope = koin.createScope<MyScope>()
        scope.get<ScopedStuff>()

        assert(koin.getOrNull<DataConsumer>() != null)
        assert(koin.getOrNull<MyDataConsumer>() != null)

        assert(koin.getOrNull<ExampleSingleton>() != null)


        stopKoin()
    }
}