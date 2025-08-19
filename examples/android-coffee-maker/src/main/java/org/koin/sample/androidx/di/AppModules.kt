package org.koin.sample.androidx.di

import android.content.Context
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.scope.Scope
import org.koin.sample.android.library.CommonModule
import org.koin.sample.androidx.multi.LibFooDModule
import org.koin.sample.androidx.repository.RepositoryModule
import org.koin.sample.clients.ClientModule

class UseContext(val context: Context)

//@KoinApplication(
//    configurations = ["default","lib"]
//)

@KoinApplication
object MyKoinApp

@Configuration
@Module(includes = [DataModule::class, LibFooDModule::class])
@ComponentScan("org.koin.sample.androidx.app")
class AppModule {

    @Factory
    fun useKoinScope(scope : Scope) : UseContext = UseContext(scope.get())
}

@Module(includes = [CommonModule::class, ClientModule::class, RepositoryModule::class])
@ComponentScan("org.koin.sample.androidx.data")
internal class DataModule
