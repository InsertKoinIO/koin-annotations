---
title: Scopes in Koin Annotations
---

While using definitions and modules, you may need to define scopes for particular space and time resolution.

## Defining a Scope with @Scope

Koin allows to use scopes, please refer to [Koin Scopes](../koin-core/scopes.md) section for more details on basics. 

To declare a scope with annotations, just use `@Scope` annotation on a class, like this

```kotlin
@Scope
class MyScopeClass
```

> this will be equivalent of the following scope section:
> ```kotlin
> scope<MyScopeClass> {
> 
>}
> ```

Else, if you need rather a scope name more than a type, you need to tag a class with `@Scope(name = )` annotation, using `name` parameter:

```kotlin
@Scope(name = "my_scope_name")
class MyScopeClass
```

> this will be the equivalent of 
>
>```kotlin
>scope<named("my_scope_name")> {
>
>}
>```

## Adding a definition in a Scope with @Scoped

To declare a definition inside a scope (defined or not with annotations), just tag a class with `@Scope` and `@Scoped` annotations:

```kotlin
@Scope(name = "my_scope_name")
@Scoped
class MyScopedComponent
```

This will generate the right definition inside the scope section:

```kotlin
scope<named("my_scope_name")> {
  scoped { MyScopedComponent() }
}
```

:::info
  You need both annotations, to indicate the needed scope space (with `@Scope`) and the kind of component to define (with `@Scoped`)
:::

## Dependency resolution from a scope

From a scoped definition, you can resolve any definition from your inner Scope and from the parents scopes.

For example, the following case will work:

```kotlin
@Single
class MySingle

@Scope(name = "my_scope_name")
@Scoped
class MyScopedComponent(
  val mySingle : MySingle,
  val myOtherScopedComponent :MyOtherScopedComponent
)

@Scope(name = "my_scope_name")
@Scoped
class MyOtherScopedComponent(
  val mySingle : MySingle
)
```

The component `MySingle` is defined as `single` definition, in root. `MyScopedComponent` and `MyOtherScopedComponent` are defined in scope "my_scope_name".
The dependencies resolution from `MyScopedComponent` is accessing Koin root with `MySingle` instance, and `MyOtherScopedComponent` scoped instance from current "my_scope_name" scope.


## Resolving outside a Scope with @ScopeId (since 1.3.0)

You may need to resolve a component from another scope, that is not directly accessible to your scope. For this you need to tag your dependency with `@ScopeId` annotation, to tell Koin to find this dependency in a scope of given scope Id.

```kotlin
@Factory
class MyFactory(
  @ScopeId("my_scope_id") val myScopedComponent :MyScopedComponent
)
```

The above code is equivalent to generate:

```kotlin
factory { Myfactory(getScope("my_scope_id").get()) }
```

This example show that `MyFactory` component will resolve `MyScopedComponent` component from a scope instance with id "my_scope_id". This scope created with id "my_scope_id" needs to be created with the right scope definition.

:::info
  The `MyScopedComponent` component needs to be defined in a Scope section, and scope instance needs to created with id "my_scope_id". 
:::