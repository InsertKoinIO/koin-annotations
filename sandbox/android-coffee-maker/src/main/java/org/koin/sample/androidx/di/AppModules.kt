package org.koin.sample.androidx.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.sample.android.library.CommonModule

@Module
@ComponentScan("org.koin.sample.androidx.app")
class AppModule

@Module(includes = [CommonModule::class])
@ComponentScan("org.koin.sample.androidx.data")
internal class DataModule