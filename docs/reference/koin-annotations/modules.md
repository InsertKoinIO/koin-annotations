---
title: Application, Configuration and Modules 
---

## Application Bootstrap with @KoinApplication

To create a complete Koin application bootstrap, you can use the `@KoinApplication` annotation on an entry point class. This annotation helps generate Koin application bootstrap functions:

```kotlin
@KoinApplication // load default configuration
object MyApp

@KoinApplication(
    configurations = ["default", "production"], 
    modules = [MyModule::class]
)
object MyApp
```

This generates **two** functions for starting your Koin application:

```kotlin
// The import below gives you access to generated extension functions
import org.koin.ksp.generated.*

fun main() {
    // Option 1: Start Koin directly
    MyApp.startKoin()
    
    // Option 2: Get KoinApplication instance
    val koinApp = MyApp.koinApplication()
}
```

Both generated functions support custom configuration:

```kotlin
fun main() {
    MyApp.startKoin {
        printLogger()
        // Add other Koin configuration
    }
    
    // Or with koinApplication
    MyApp.koinApplication {
        printLogger()
    }
}
```

The `@KoinApplication` annotation supports:
- `configurations`: Array of configuration names to scan and load
- `modules`: Array of module classes to include directly (in addition to configurations)

:::info
When no configurations are specified, it automatically loads the "default" configuration.
:::

## Configuration Management with @Configuration

The `@Configuration` annotation allows you to organize modules into different configurations (environments, flavors, etc.). This is useful for organizing modules by deployment environment or feature sets.

### Basic Configuration Usage

```kotlin
// Put module in default Configuration
@Module
@Configuration
class CoreModule
```

:::info
The default configuration is named "default", can be used with `@Configuration` or `@Configuration("default")`
:::

You need to use the `@KoinApplication` to be able to scan modules from configuration:

```kotlin
// module A
@Module
@Configuration
class ModuleA

// module B
@Module
@Configuration
class ModuleB

// module App, scan all @Configuration modules
@KoinApplication
object MyApp
```


### Multiple Configuration Support

A module can be associated with multiple configurations:

```kotlin
// This module is available in both "prod" and "test" configurations
@Module
@Configuration("prod", "test")
class DatabaseModule {
    @Single
    fun database() = PostgreSQLDatabase()
}

// This module is available in default, test, and development
@Module
@Configuration("default", "test", "development") 
class LoggingModule {
    @Single
    fun logger() = Logger()
}
```

### Environment-Specific Configurations

```kotlin
// Development-only configuration
@Module
@Configuration("development")
class DevDatabaseModule {
    @Single
    fun database() = InMemoryDatabase()
}

// Production-only configuration  
@Module
@Configuration("production")
class ProdDatabaseModule {
    @Single
    fun database() = PostgreSQLDatabase()
}

// Available in multiple environments
@Module
@Configuration("default", "production", "development")
class CoreModule {
    @Single
    fun logger() = Logger()
}
```

### Using Configurations with @KoinApplication

By default, the `@KoinApplication` is loading all default configurations (modules tagged with `@Configuration`)

You can also reference these configurations in your application bootstrap:

```kotlin
@KoinApplication(configurations = ["default", "production"])
class ProductionApp

@KoinApplication(configurations = ["default", "development"])  
class DevelopmentApp

// Load only default configuration (same as @KoinApplication with no parameters)
@KoinApplication
class SimpleApp
```

:::info
- Empty `@Configuration` is equivalent to `@Configuration("default")`
- The "default" configuration is loaded automatically when no specific configurations are specified
- Modules can belong to multiple configurations by listing them in the annotation
:::


## Default Module (Deprecated since 1.3.0)

:::warning
The default module approach is deprecated since Annotations 1.3.0. We recommend using explicit modules with `@Module` and `@Configuration` annotations for better organization and clarity.
:::

While using definitions, you may need to organize them in modules or not. Previously, you could use the "default" generated module to host definitions without explicit modules.

If you don't want to specify any module, Koin provides a default one to host all your definitions. The `defaultModule` is ready to be used directly:

```kotlin
// The import below gives you access to generated extension functions
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

**Recommended approach**: Instead of using the default module, organize your definitions in explicit modules:

```kotlin
@Module
@Configuration
class MyModule {
    // Your definitions here
}

// Then use @KoinApplication
@KoinApplication
object MyApp
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

To load your module in Koin, just use the `.module` extension generated for any `@Module` class. Just create a new instance of your module `MyModule().module`:

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

This will scan the current package and subpackages for annotated components. You can specify to scan a given package with `@ComponentScan("com.my.package")`

:::info
When using `@ComponentScan` annotation, KSP traverses across all Gradle modules for the same package. (since 1.4)
:::

## Definitions in Class Modules

To define a definition directly in your code, you can annotate a function with definition annotations:

```kotlin
// given 
// class MyComponent(val myDependency : MyDependency)

@Module
class MyModule {

  @Single
  fun myComponent(myDependency : MyDependency) = MyComponent(myDependency)
}
```

> **Note**: `@InjectedParam` (for injected parameters from startKoin) and `@Property` (for property injection) are also usable on function members. See the definitions documentation for more details on these annotations.


## Including Modules

To include other class modules in your module, use the `includes` attribute of the `@Module` annotation:

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
