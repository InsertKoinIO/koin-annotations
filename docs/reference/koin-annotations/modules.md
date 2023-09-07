---
title: Modules with @Module 
---

While using definitions, you may need to organize them in modules or not. You can even not use any module at all and use the "default" generated module. 

## No Module - Using the Generated Default Module

If you don't want to specify any module, Koin provide a default one to host all your definitions. The `defaultModule` is ready to be use directly:

```kotlin
// Use Koin Generation
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        defaultModule()
    }
}

// or 

fun main() {
    startKoin {
        modules(
          defaultModule
        )
    }
}
```

:::info
  Don't forget to use the `org.koin.ksp.generated.*` import
:::

## Class Module with @Module

To declare a module, just tag a class with `@Module` annotation:

```kotlin
@Module
class MyModule
```

To load your module in Koin, just use the `.module` extension generated for any `@Module` class. Just create new instance of your module `MyModule().module`:

```kotlin
// Use Koin Generation
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        modules(
          MyModule().module
        )
    }
}
```

> Don't forget to use the `org.koin.ksp.generated.*` import

## Components Scan with @ComponentScan

To scan and gather annotated components into a module, just use the `@ComponentScan` annotation on a module:

```kotlin
@Module
@ComponentScan
class MyModule
```

This will scan current package and subpackages for annotated components. You can specify to scan a given package `@ComponentScan("com.my.package")`

:::info
  When using `@ComponentScan` annotation, KSP will scan in the current Gradle sources only, not accross multiple modules.
:::


## Definitions in Class Modules

To define a definition directly in your can, you an annotate a function with definition annotations:

```kotlin
// given 
// class MyComponent(val myDependency : MyDependency)

@Module
class MyModule {

  @Single
  fun myComponent(myDependency : MyDependency) = MyComponent(myDependency)
}
```

> @InjectedParam, @Property are also usable on function members 


## Including Modules

To include other class modules to your module, just use the `includes` attribute of the `@Module` annotation:

```kotlin
@Module
class ModuleA

@Module(includes = [ModuleA::class])
class ModuleB
```

This way you can just run your root module:


```kotlin
// Use Koin Generation
import org.koin.ksp.generated.*

fun main() {
    startKoin {
        modules(
          // will load ModuleB & ModuleA
          ModuleB().module
        )
    }
}
```
