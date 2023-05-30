package org.koin.example.newmodule

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.`interface`.MyInterfaceExt

public class MyOtherComponent2(public val i : MyInterfaceExt)

@Module
public class MyModule2 {

    @Single
    public fun myOtherComponent2(i: MyInterfaceExt): MyOtherComponent2 = MyOtherComponent2(i)
}