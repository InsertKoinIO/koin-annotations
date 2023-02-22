package org.koin.example.newmodule

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.`interface`.MyInterfaceExt

class MyOtherComponent2(val i : MyInterfaceExt)

@Single
fun createMyOtherComponent(i: MyInterfaceExt) = MyOtherComponent2(i)

@Module
@ComponentScan
class MyModule