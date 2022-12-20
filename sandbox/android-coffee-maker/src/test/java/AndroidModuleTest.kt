package org.koin.sample.androidx

import org.junit.Test
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ksp.generated.defaultModule
import org.koin.ksp.generated.module
import org.koin.sample.androidx.di.AppModule
import org.koin.sample.androidx.di.DataModule
import org.koin.sample.androidx.repository.RepositoryModule

class AndroidModuleTest {

    @Test
    fun run_all_modules() {
        startKoin {
            modules(
                defaultModule,
                DataModule().module,
                RepositoryModule().module,
                AppModule().module,
            )
        }
        stopKoin()
    }
}