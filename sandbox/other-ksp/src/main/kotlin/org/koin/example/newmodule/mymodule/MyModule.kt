package org.koin.example.newmodule.mymodule

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.`interface`.MyInterfaceExt

@Single(createdAtStart = true)
public class MyOtherComponent3(public val i : MyInterfaceExt)

@Module
@ComponentScan
public class MyModule3