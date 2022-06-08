# Koin Annotations

The goal of Koin Annotations project is to help declare Koin definition in a very fast and intuitive way, and generate all underlying Koin DSL for you. The goal is to help developer experience to scale and go fast 🚀, thanks to Kotlin Compilers.

## Current Version

Here below is the current version:

```kotlin
koin_annotations_version = "1.0.0"
```

> Koin 3.2+ is required

## Setup

First, setup KSP plugin like this, in your root `build.gradle`:

```kotlin
plugins {
    id "com.google.devtools.ksp" version "1.6.21-1.0.5"
}
```

Use the following dependencies in your Gradle dependencies section:

```kotlin
// Koin Annotations
implementation "io.insert-koin:koin-annotations:$koin_annotations_version"

// Koin Annotations - Ksp Compiler
ksp "io.insert-koin:koin-ksp-compiler:$koin_annotations_version"
```

On your app add the following to generated source code:

* On Kotlin project:

```groovy
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}
```

* on Android project:

```groovy
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

## QuickStart

Below some quickstart apps:
* [Kotlin app](https://github.com/InsertKoinIO/koin-annotations/tree/main/quickstart/quickstart-kotlin-annotations)
* [Android app](https://github.com/InsertKoinIO/koin-annotations/tree/main/quickstart/quickstart-android-annotations)

## [Documentation](https://insert-koin.io/docs/reference/koin-annotations/annotations)
