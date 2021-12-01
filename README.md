# Koin Annotations & Kotlin Compilers

The goal of Koin compiler & Annotations project is to help declare Koin definition in a very fast and intuitive way, and generate all underlying Koin DSL for you. The goal is to help developer experience to scale and go fast 🚀.

## Current Version

Here below is the current version:

> Warning: while this is still in beta. There can be some breaking changes coming in next releases 🙏

```kotlin
// Koin KSP Compiler
koinKspVersion = "1.0.0-beta-1"
```

## Setup

First, setup KSP plugin like this, in your root `build.gradle`:

```kotlin
plugins {
    id "com.google.devtools.ksp" version "1.5.30-1.0.0"
}
```

Use the following dependencies in your Gradle dependencies section:

```kotlin
// Koin Annotations
implementation "io.insert-koin:koin-annotations:$koinKspVersion"
// Koin Ksp Compiler
ksp "io.insert-koin:koin-ksp-compiler:$koinKspVersion"
```

On Android add the following to generated source code:

```kotlin
android {
  applicationVariants.all { variant ->
          variant.sourceSets.java.each {
              it.srcDirs += "build/generated/ksp/${variant.name}/kotlin"
          }
      }
}
```

## Getting Started

Not familiar with Koin? First take a look at [Koin Getting Started](https://insert-koin.io/docs/quickstart/kotlin)

Tag your cpomnents with definition & module annotations, and use the regular Koin API.

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

## [QuickStart Samples]()

## [Documentation]()
