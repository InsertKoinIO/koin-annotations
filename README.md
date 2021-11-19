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

## QuickStart Samples

You can find below some example application to help you start:
- [Coffee Maker App]()
- [Android Coffee Maker App]()

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

---

## Definitions

Koin Annotations allow to declare the same kind of definitions as the regular Koin DSL, but with annotations. Just tag your class with the needed annotatation, and it will generate everything for you!

For example the equivalent to `single { MyComponent(get()) }` DSL declaration, is just done by tagging with `@Single` like this:

```kotlin
@Single
class MyComponent(val myDependency : MyDependency)
```

Koin Annotations keep the same semantic as the Koin DSL. You can declare your components with the following definitions:

- `@Single` - singleton instance
- `@Factory` - factory instance (recreated each time you need an instance)
- `@KoinViewModel` - Android ViewModel instance

For Scopes, check the [Declaring Scopes]() section.

### Automatic or Specific Binding

When declaring a component, all detected "bindings" (associated supertypes) will be already prepared for you. For example, the following definition:

```kotlin
@Single
class MyComponent(val myDependency : MyDependency) : MyInterface
```

Koin will declare that your `MyComponent` component is also tied to `MyInterface`. The DSL equivalent is `single { MyComponent(get()) } bind MyInterface::class`.


Instead of letting Koin detect thigns for you, you can also specify what type you really want to bind with the `binds` annotation parameter:

 ```kotlin
@Single(binds = [MyBoundType::class])
```

### Nullable Dependencies

If your component is using nullable depndency, don't worry it will be handled automaticaly for you. Keep using yopur definition annotation, and Koin will guess what to do:

```kotlin
@Single
class MyComponent(val myDependency : MyDependency?)
```

The generated DSL equivalent will be `single { MyComponent(getOrNull()) }`


> Note that this also works for injected Parameters and properties

### Qualifier with @Named

You can add a "name" to definition (also called qualifier), to make distinction between several definitions for the same type, with the `@Named` annotation:

```kotlin
@Single
@Named("InMemoryLogger")
class LoggerInMemoryDataSource : LoggerDataSource

@Single
@Named("DatabaseLogger")
class LoggerLocalDataSource(private val logDao: LogDao) : LoggerDataSource
```

When resolving a dependency, just use the qualifier with `named` function:

```kotlin
val logger: LoggerDataSource by inject(named("InMemoryLogger"))
```

### Injected Parameters with @InjectedParam

You can tag a constructor member as "injected parameter", which means that the dependency will be passed in the graph when calling for resolution.

For example:

```kotlin
@Single
class MyComponent(@InjectedParam val myDependency : MyDependency)
```

Then you can call your `MyComponent` and pass a instance of `MyDependency`:

```kotlin
val m = MyDependency
// Resolve MyComponent while passing  MyDependency
koin.get<MyComponent> { parametersOf(m) }
```

The generated DSL equivalent will be `single { params -> MyComponent(params.get()) }`

### Properties with @Property

To resolve a Koin property in your definition, just tag a cosntructor member with `@Property`. Ths is will resolve the Koin property thanks to the value passed to the annotation:

```kotlin
@Single
class MyComponent(@Property("my_key") val myProperty : String)
```

The generated DSL equivalent will be `single { MyComponent(getProperty("my_key")) }`

### Declaring Scopes with @Scope

You can declare definition inside a scope, by using the `@Scope` annotation. The target scope can be specified as a class, or a name:

```kotlin
// scope by type
@Scope(MyScope::class)
class MyComponent

// scope by name
@Scope(name = "MyScopeName")
class MyComponent
```

You can cumulate `@Factory` or `@KoinViewModel`, to specify a scoped Factory or a ViewModel.   

The generated DSL equivalent will be:

```kotlin
scope<MyScope> {
  scoped { MyComponent() }
}
// or
scope(named("MyScopeName")) {
  scoped { MyComponent() }
}
```

---

## Modules

While using definitions, you may need to organize them in modules or not. You can even not use any module at all and use the "default" generated module. 

### No Module - Using the Generated Default Module

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

> Don't forget to use the `org.koin.ksp.generated.*` import

### Class Module with @Module

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

### Components Scan with @ComponentScan

To scan and gather annotated components into a module, just use the `@ComponentScan` annotation on a module:

```kotlin
@Module
@ComponentScan
class MyModule
```

This will scan current package and subpackages for annotated components.

> You can specify to scan a given package `@ComponentScan("com.my.package")`


### Definitions in Class Modules

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

