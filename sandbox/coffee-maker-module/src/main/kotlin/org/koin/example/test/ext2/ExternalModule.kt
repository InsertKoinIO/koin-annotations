package org.koin.example.test.ext2

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single


class MyExt2Component()

@Module
class ExternalModule {

    @Single
    fun myExt2Component() = MyExt2Component()
}