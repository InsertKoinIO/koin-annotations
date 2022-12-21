---
title: Starting with Annotations
---

The goal of Koin Annotations project is to help declare Koin definition in a very fast and intuitive way, and generate all underlying Koin DSL for you. The goal is to help developer experience to scale and go fast 🚀, thanks to Kotlin Compilers.

## Getting Started

Not familiar with Koin? First take a look at [Koin Getting Started](https://insert-koin.io/docs/quickstart/kotlin)

Tag your components with definition & module annotations, and use the regular Koin API.

```kotlin
// Tag your component to declare a definition
@Single
class MyComponent
```

```kotlin
// Declare a module and scan for annotations
@Module
@ComponentScan
class MyModule
```

Use the `org.koin.ksp.generated.*` import as follow to be able to use generated code:

```kotlin
// Use Koin Generation
import org.koin.ksp.generated.*

fun main() {
    val koin = startKoin {
        printLogger()
        modules(
          // use your modules here, with generated ".module" extension on Module classes
          MyModule().module
        )
    }

    // Just use your Koin API as regular
    koin.get<MyComponent>()
}
```

That's it, you can use your new definitions in Koin with the [regular Koin API](https://insert-koin.io/docs/reference/introduction)


### Kotlin KMP Setup

Please follow KSP setup as described in official documentation: [KSP with Kotlin Multiplatform](https://kotlinlang.org/docs/ksp-multiplatform.html)

You can also check the [Hello Koin KMP](https://github.com/InsertKoinIO/hello-kmp/tree/annotations) project with basic setup for Koin Annotations.
