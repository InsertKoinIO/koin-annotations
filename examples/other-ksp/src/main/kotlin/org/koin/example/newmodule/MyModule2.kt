package org.koin.example.newmodule

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.`interface`.MyInterfaceExt

public class MyOtherComponent2(public val i: MyInterfaceExt)
public class MyOtherComponent3F(public val c2: MyOtherComponent2)

@Module
@Configuration
public class MyModule2 {

    @Single
    public fun myOtherComponent2(i: MyInterfaceExt): MyOtherComponent2 = MyOtherComponent2(i)

    @Single
    public fun myOtherComponent3F(c2: MyOtherComponent2): MyOtherComponent3F = MyOtherComponent3F(c2)
}