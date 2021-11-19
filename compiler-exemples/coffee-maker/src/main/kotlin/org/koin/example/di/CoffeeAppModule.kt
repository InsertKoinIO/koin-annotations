package org.koin.example.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("org.koin.example.coffee")
class CoffeeAppModule
