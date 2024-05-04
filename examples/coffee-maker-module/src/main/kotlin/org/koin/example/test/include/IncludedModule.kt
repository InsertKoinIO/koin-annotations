package org.koin.example.test.include

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

class IncludedComponent

@Module
class IncludedModule {

    @Single
    fun includedComponent() = IncludedComponent()
}