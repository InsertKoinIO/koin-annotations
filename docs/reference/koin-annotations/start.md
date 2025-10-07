---
title: Starting with Koin Annotations
---

The goal of the Koin Annotations project is to help declare Koin definitions in a fast and intuitive way, and generate all underlying Koin DSL for you. The goal is to help developers experience scaling and go fast 🚀, thanks to Kotlin Compilers.

## Getting Started

Not familiar with Koin? First, take a look at [Koin Getting Started](https://insert-koin.io/docs/quickstart/kotlin/)

Tag your components with definition & module annotations, and use the regular Koin API.

```kotlin
// Tag your component to declare a definition
@Single
class MyComponent
```

### Basic Module Setup

```kotlin
// Declare a module and scan for annotations
@Module
class MyModule
```

Now you can start your Koin application with `@KoinApplication` and explicitly specify the modules to use:

```kotlin
// The import below gives you access to generated extension functions
// like MyModule.module and MyApp.startKoin() 
import org.koin.ksp.generated.*

@KoinApplication(modules = [MyModule::class])
object MyApp

fun main() {
    MyApp.startKoin {
        printLogger()
    }

    // Just use your Koin API as regular
    KoinPlatform.getKoin().get<MyComponent>()
}
```

### Configuration-based Module Setup

Alternatively, you can use `@Configuration` to create modules that are automatically loaded:

```kotlin
// Module with configuration - automatically included in default config
@Module
@Configuration
class MyModule
```

With configuration, you don't need to specify modules explicitly:

```kotlin
// The import below gives you access to generated extension functions
// This approach loads all modules marked with @Configuration automatically
import org.koin.ksp.generated.*

@KoinApplication
object MyApp

fun main() {
    MyApp.startKoin {
        printLogger()
    }

    // Just use your Koin API as regular
    KoinPlatform.getKoin().get<MyComponent>()
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

The compiler will check that all dependencies used in your configuration are declared, and all used modules are accessible.

### Bypass Compile Safety with @Provided (since 1.4.0)

Among the ignored types from the Compiler (Android common types), the compiler plugin can verify your Koin configuration at compile time. If you want to exclude a parameter from being checked, you can use `@Provided` on a parameter to indicate that this type is provided externally to the current Koin Annotations config.

The following indicates that `MyProvidedComponent` is already declared in Koin:

```kotlin
class MyProvidedComponent

@Factory
class MyPresenter(@Provided val provided : MyProvidedComponent)
```

### Default Module (Deprecated since 1.3.0)

:::warning
The default module approach is deprecated since Annotations 1.3.0. We recommend using explicit modules with `@Module` and `@Configuration` annotations for better organization and clarity.
:::

Previously, the Koin compiler would detect any definition not bound to a module and put it in a "default module". This approach is now deprecated in favor of using `@Configuration` and `@KoinApplication` annotations.

**Deprecated approach** (avoid using):
```groovy
// in build.gradle or build.gradle.kts

ksp {
    arg("KOIN_DEFAULT_MODULE","true")
}
```

**Recommended approach**: Use explicit module organization as shown in the examples above with `@Configuration` and `@KoinApplication`.

### Kotlin KMP Setup

Please follow the KSP setup as described in the official documentation: [KSP with Kotlin Multiplatform](https://kotlinlang.org/docs/ksp-multiplatform.html)

You can also check the [Hello Koin KMP](https://github.com/InsertKoinIO/hello-kmp/tree/annotations) project with a basic setup for Koin Annotations.

### Pro-Guard

If you intend to embed the Koin Annotations application as an SDK, take a look at those pro-guard rules:

```
# Keep annotation definitions
-keep class org.koin.core.annotation.** { *; }

# Keep classes annotated with Koin annotations  
-keep @org.koin.core.annotation.* class * { *; }
```
