---
title: Definitions with Annotations
---


Koin Annotations allow to declare the same kind of definitions as the regular Koin DSL, but with annotations. Just tag your class with the needed annotation, and it will generate everything for you!

For example the equivalent to `single { MyComponent(get()) }` DSL declaration, is just done by tagging with `@Single` like this:

```kotlin
@Single
class MyComponent(val myDependency : MyDependency)
```

Koin Annotations keep the same semantic as the Koin DSL. You can declare your components with the following definitions:

- `@Single` - singleton instance (declared with `single { }` in DSL)
- `@Factory` - factory instance. For instances recreated each time you need an instance. (declared with `factory { }` in DSL)
- `@KoinViewModel` - Android ViewModel instance (declared with `viewModel { }` in DSL)

For Scopes, check the [Declaring Scopes](/docs/reference/koin-core/scopes.md) section.

### Generate Compose ViewModel for Kotlin Multipaltform (since 1.4.0)

The `@KoinViewModel` annotation can be used to generate either Android or Compsoe KMP ViewModel. To generate `viewModel` Koin definition with `org.koin.compose.viewmodel.dsl.viewModel` instead of regular `org.koin.androidx.viewmodel.dsl.viewModel`, you need to activate the `USE_COMPOSE_VIEWMODEL` option:  

```groovy
ksp {
    arg("USE_COMPOSE_VIEWMODEL","true")
}
```

:::note
    Koin 4.0 should bring merge of those 2 ViewModel DSL into only one, as the ViewModel type argiument comes from teh same library
:::

## Automatic or Specific Binding

When declaring a component, all detected "bindings" (associated supertypes) will be already prepared for you. For example, the following definition:

```kotlin
@Single
class MyComponent(val myDependency : MyDependency) : MyInterface
```

Koin will declare that your `MyComponent` component is also tied to `MyInterface`. The DSL equivalent is `single { MyComponent(get()) } bind MyInterface::class`.


Instead of letting Koin detect things for you, you can also specify what type you really want to bind with the `binds` annotation parameter:

 ```kotlin
@Single(binds = [MyBoundType::class])
```

## Nullable Dependencies

If your component is using nullable dependency, don't worry it will be handled automatically for you. Keep using your definition annotation, and Koin will guess what to do:

```kotlin
@Single
class MyComponent(val myDependency : MyDependency?)
```

The generated DSL equivalent will be `single { MyComponent(getOrNull()) }`


> Note that this also works for injected Parameters and properties

## Qualifier with @Named

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

## Injected Parameters with @InjectedParam

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


## Injecting a lazy dependency - `Lazy<T>`

Koin can automatically detect and resolve a lazy dependency. Here for example, we want to resolve lazily the `LoggerDataSource` definition. You just need to use the `Lazy` Kotlin type like follow:

```kotlin
@Single
class LoggerInMemoryDataSource : LoggerDataSource

@Single
class LoggerAggregator(val lazyLogger : Lazy<LoggerDataSource>)
```

Behind it will generate the DSL like with `inject()` instead of `get()`:

```kotlin
single { LoggerAggregator(inject()) }
```

## Injecting a list of dependencies - `List<T>`

Koin can automatically detect and resolve all a list of dependency. Here for example, we want to resolve all `LoggerDataSource` definition. You just need to use the `List` Kotlin type like follow:

```kotlin
@Single
@Named("InMemoryLogger")
class LoggerInMemoryDataSource : LoggerDataSource

@Single
@Named("DatabaseLogger")
class LoggerLocalDataSource(private val logDao: LogDao) : LoggerDataSource

@Single
class LoggerAggregator(val datasource : List<LoggerDataSource>)
```

Behind it will generate the DSL like with `getAll()` function:

```kotlin
single { LoggerAggregator(getAll()) }
```

## Properties with @Property

To resolve a Koin property in your definition, just tag a constructor member with `@Property`. Ths is will resolve the Koin property thanks to the value passed to the annotation:

```kotlin
@Factory
public class ComponentWithProps(
    @Property("id") public val id : String
)
```

The generated DSL equivalent will be `factory { ComponentWithProps(getProperty("id")) }`

### @PropertyValue - Property with default value (since 1.4)

Koin Annotations offers you the possibility to define a default value for a property, directly from your code with `@PropertyValue` annotation.
Let's follow our sample:

```kotlin
@Factory
public class ComponentWithProps(
    @Property("id") public val id : String
){
    public companion object {
        @PropertyValue("id")
        public const val DEFAULT_ID : String = "_empty_id"
    }
}
```

The generated DSL equivalent will be `factory { ComponentWithProps(getProperty("id", ComponentWithProps.DEFAAULT_ID)) }`
