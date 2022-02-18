package org.koin.sample

import androidx.lifecycle.SavedStateHandle
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.koin.dsl.koinApplication
import org.koin.ksp.generated.module
import org.koin.sample.di.AndroidAppModule
import org.koin.test.KoinTest
import org.koin.test.category.CheckModuleTest
import org.koin.test.check.checkModules
import org.koin.test.mock.MockProviderRule
import org.mockito.Mockito

@Category(CheckModuleTest::class)
class CheckModulesTest : KoinTest {

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        Mockito.mock(clazz.java)
    }

    @Test
    fun checkAllModules() {
        koinApplication {
            modules(AndroidAppModule().module)
            checkModules {
                withInstance<SavedStateHandle>()
            }
        }
    }
}