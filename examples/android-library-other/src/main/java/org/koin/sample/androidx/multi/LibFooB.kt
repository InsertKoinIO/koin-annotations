package org.koin.sample.androidx.multi

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.example.multi.FooBase

@Module
@Configuration
@ComponentScan
class LibFooBModule

@Factory
class FooB : FooBase() {
    val text = "text B"
}

@Factory
class FooC : FooBase() {
    val text = "text C"
}