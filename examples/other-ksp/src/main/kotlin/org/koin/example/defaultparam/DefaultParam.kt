package org.koin.example.defaultparam

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module

@Module
@Configuration
@ComponentScan
public class MyModule

public const val COMPONENT_DEFAULT: String = "default"

@Factory
public class Component(@InjectedParam public val param: String = COMPONENT_DEFAULT)

