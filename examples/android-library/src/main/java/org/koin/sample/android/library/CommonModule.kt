package org.koin.sample.android.library

import it.example.component.ExampleModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [ExampleModule::class])
@ComponentScan
class CommonModule