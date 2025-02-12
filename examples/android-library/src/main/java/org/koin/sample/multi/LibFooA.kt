package org.koin.sample.multi

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.example.multi.FooBase

@Module
@ComponentScan
class LibFooAModule

@Factory
class FooA: FooBase(){
    val text  = "text A"
}