package org.koin.example.newmodule

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.`interface`.MyInterfaceExt

class MyOtherComponent2(val i : MyInterfaceExt)

@Module
class MyModule2 {

    @Single
    fun myOtherComponent2(i: MyInterfaceExt) = MyOtherComponent2(i)
}