package org.koin.example

// be sure to import "import org.koin.ksp.generated.*"
import org.koin.core.annotation.Single
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.example.`interface`.MyInterfaceExt
import org.koin.example.newmodule.MyModule2
import org.koin.example.newmodule.mymodule.MyModule3
import org.koin.ksp.generated.*


@Single
public class MyComponent : MyInterfaceExt

public class MyOtherComponent(public val i: MyInterfaceExt)

@Single
public fun createMyOtherComponent(i: MyInterfaceExt) : MyOtherComponent= MyOtherComponent(i)


public fun main() {
    startKoin {
        printLogger()
        // else let's use our modules
        modules(
            defaultModule, MyModule3().module, MyModule2().module,
        )
    }
}