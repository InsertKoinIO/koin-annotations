package org.koin.sample.androidx.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("org.koin.sample.androidx.app")
class AppModule

@Module
@ComponentScan("org.koin.sample.androidx.data")
class DataModule