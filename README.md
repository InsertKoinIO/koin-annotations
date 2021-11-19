# Koin Kotlin Compilers

The goal of Koin compiler & Annotations project is to help declare Koin definition in a very fast and intuitive way, and generate all underlying Koin DSL for you. The goal is to help developer experience to scale and go fast 🚀.

## Current Version

Here below is the current version:

```kotlin
koinKspVersion = "1.0.0-alpha-1"
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
implementation "io.insert-koin:koin-annotations:$koinKspVersion"
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

## QuickStart

You can find below some example application to help you start:
- [Coffee Maker App]()
- [Android Coffee Maker App]()

## Getting Started

The only thing to setup in particular is the `org.koin.ksp.generated.*` import as follow, to be able to use generated code:

```kotlin
import org.koin.ksp.generated.*

fun main() {
    val koin = startKoin {
        printLogger()
        modules(
          // use your modules here, with generated ".module" extension on classes
          MyModule().module
        )
    }

    // Just use your Koin API as regular
    koin.get<MyComponent>()
}

// Declare a module and scan for annotations
@Module
@ComponentScan
class MyModule()

// Declare a Single instance for MyComponent
@Single
class MyComponent()
```

Just tag your code with definition & module annotations, and use the regular Koin API.

## Definitions

### Koin definitions with Annotations

### Automatic Binding

### Nullable Injection

### Qualifier

### injected Parameter

### Property

### Scope

## Modules

### Default Module

### Class Module

### Components Scan
