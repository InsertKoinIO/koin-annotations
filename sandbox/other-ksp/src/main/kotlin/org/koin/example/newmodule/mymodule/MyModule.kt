package org.koin.example.newmodule.mymodule

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.`interface`.MyInterfaceExt

@Single
class MyOtherComponent3(val i : MyInterfaceExt)

@Module
@ComponentScan
class MyModule3