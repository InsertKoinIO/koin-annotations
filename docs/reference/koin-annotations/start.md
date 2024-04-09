---
title: Starting with Koin Annotations
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

## KSP Options

The Koin compiler offers some options to configure. Following the official doc, you can add the following options to your project: [Ksp Quickstart Doc](https://kotlinlang.org/docs/ksp-quickstart.html#pass-options-to-processors)

### Compile Safety - check your Koin config at compile time (since 1.3.0)

Koin Annotations allows the compiler plugin to verify your Koin configuration at compile time. This can be activated with the following Ksp options, to add to your Gradle module: 

```groovy
// in build.gradle or build.gradle.kts

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}
```

The compiler will check that all dependencies used in your configuration is declared, and all used modules are accessible.

:::note
  this feature is still experimental
:::

### Disabling Default Module (since 1.3.0) 

By default, the Koibn compiler detect any definition not bound to a module and put it in a "default mmodule", a Koin module generated at the root of your project. You can disable the use and generation of default module with the following option:

```groovy
// in build.gradle or build.gradle.kts

ksp {
    arg("KOIN_DEFAULT_MODULE","false")
}
```

### Kotlin KMP Setup

Please follow KSP setup as described in official documentation: [KSP with Kotlin Multiplatform](https://kotlinlang.org/docs/ksp-multiplatform.html)

You can also check the [Hello Koin KMP](https://github.com/InsertKoinIO/hello-kmp/tree/annotations) project with basic setup for Koin Annotations.
