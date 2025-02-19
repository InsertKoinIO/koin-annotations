package org.koin.sample.androidx.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.sample.android.library.CommonModule
import org.koin.sample.androidx.multi.LibFooBModule
import org.koin.sample.androidx.repository.RepositoryModule
import org.koin.sample.clients.ClientModule
import org.koin.sample.multi.LibFooAModule

@Module(includes = [DataModule::class, LibFooAModule::class, LibFooBModule::class])
@ComponentScan("org.koin.sample.androidx.app")
class AppModule


@Module(includes = [CommonModule::class, ClientModule::class, RepositoryModule::class])
@ComponentScan("org.koin.sample.androidx.data")
internal class DataModule
