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
// Use the generated application functions
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
// Default configuration - these are equivalent
@Module
@Configuration
class CoreModule

@Module  
@Configuration("default")
class CoreModule
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

Reference these configurations in your application bootstrap:

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


## No Module - Using the Generated Default Module (Deprecation since Annotations 2.2)

While using definitions, you may need to organize them in modules or not. You can even not use any module at all and use the "default" generated module.
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
When using `@ComponentScan` annotation, KSP traverses accross all Gradle modules for the same package. (since 1.4)
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
