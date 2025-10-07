package org.koin.example.injparam

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module

@Module
@Configuration
@ComponentScan
public class InjectedParamModule

@Factory
public class MyInjectFactory(@InjectedParam public val ints: List<Int>)