package org.koin.sample.androidx.multi

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.example.multi.FooBase

@Module
@ComponentScan
class LibFooDModule

@Factory
class FooD : FooBase() {
    val text = "text D"
}