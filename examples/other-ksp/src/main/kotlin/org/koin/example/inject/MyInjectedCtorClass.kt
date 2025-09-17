package org.koin.example.inject

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@ComponentScan
@Configuration
class MyCompatModule

@Singleton
class MySingleton

class MyInjectedCtorClass @Inject constructor(val s : MySingleton)