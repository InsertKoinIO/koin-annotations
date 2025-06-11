---
title: Kotlin Multiplatform - Definitions and Modules Annotations
---

## KSP Setup

Please follow KSP setup as described in official documentation: [KSP with Kotlin Multiplatform](https://kotlinlang.org/docs/ksp-multiplatform.html)

You can also check the [Hello Koin KMP](https://github.com/InsertKoinIO/hello-kmp/tree/annotations) project with basic setup for Koin Annotations.

Add the KSP Plugin

```kotlin
plugins {
    alias(libs.plugins.ksp)
}
```

Use annotations library in common API:

```kotlin
sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            api(libs.koin.annotations)
            // ...
        }
}
```

And don't forget to configure KSP on right sourceSet:

```kotlin
dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}
```

## Defining Definitions and Modules in common code, and Manage native parts

In your commonMain sourceSet, declare your Module, scan for definitions, or define functions as a regular declaration. See [Definitions](definitions.md) and [Modules](./modules.md).

Below we have a `PlatformModule`, scanning in `com.jetbrains.kmpapp.platform` package where we have `PlatformHelper` class. The module class is annotated with `@Module` and `@ComponentScan` annotations.

```kotlin
// in commonMain

@Module
@ComponentScan("com.jetbrains.kmpapp.platform")
class PlatformModule

// package com.jetbrains.kmpapp.platform 
@Single
class PlatformHelper {
    fun getName() : String = "I'm a common Platform"
}
```

In a Kotlin Multiplatform application, some components will need to be implemented specifically per platform. You can share those components at the definition level, with expected/actual on the given class (definition or module). 
You can share a definition with expect/actual implementation, or a module with expect/actual.

### Sharing Definition - Scanning Expect/Actual class definition from common sources

From your commonMain code sourceSet, you can scan for expect classes that will have their own implementation on each native platform. Be aware to use `expect/actual` definitions, even on constructors if needed (not using default constructor).

:::note
You scan for definitions in commonMain sources. Each expect class, has its implementation in each native source folder.
:::

In commonMain:
```kotlin
// commonMain

@Module
@ComponentScan("com.jetbrains.kmpapp.native")
class NativeModuleA()

// package com.jetbrains.kmpapp.native
@Factory
expect class PlatformComponentA() {
    fun sayHello() : String
}
```

In native sources:

```kotlin
// androidMain

// package com.jetbrains.kmpapp.native
actual class PlatformComponentA {
    actual fun sayHello() : String = "I'm Android - A"
}

// iOSMain

// package com.jetbrains.kmpapp.native
actual class PlatformComponentA {
    actual fun sayHello() : String = "I'm iOS - A"
}
```

### Sharing Definition - Declaring Expect/Actual function definition from common module

From your commonMain code sourceSet, you can define an expect class definition with their own implementation on each native platform. Be aware to use `expect/actual` definitions, even on constructors.

:::note
You declare definitions in commonMain sources, for given common module. Each expect class, has its implementation in each native source folder.
:::

In commonMain:
```kotlin
// commonMain

@Module
class NativeModuleB {

    @Factory
    fun providesPlatformComponentB() : PlatformComponentB = PlatformComponentB()
}

// package com.jetbrains.kmpapp.native
expect class PlatformComponentB() {
    fun sayHello() : String
}
```

In native sourceSets:

```kotlin
// androidMain

// package com.jetbrains.kmpapp.native
actual class PlatformComponentB {
    actual fun sayHello() : String = "I'm Android - B"
}

// iOSMain

// package com.jetbrains.kmpapp.native
actual class PlatformComponentB {
    actual fun sayHello() : String = "I'm iOS - B"
}
```

### Sharing Module - Declaring pure native Module, using expect/actual for Module class

:::note
You declare a common class Module, with expect/actual. Each platform implementation will scan or declare its own definitions
:::

:::info
No definitions are declared in the common Module! Else it won't scan for native components.
:::

In commonMain:
```kotlin
// commonMain

@Module
expect class NativeModuleC()
```

In native sourceSets:

```kotlin
// androidMain

@Module
@ComponentScan("com.jetbrains.kmpapp.other.android")
actual class NativeModuleC

// package com.jetbrains.kmpapp.other.android
@Factory 
class PlatformComponentC(val context: Context) {
    fun sayHello() : String = "I'm Android - C - $context"
}
```

:::note
Your module needs to not have any definition from the commonMain source set, to be considered as used only on the platform. 
:::


### Sharing Definition - Dynamically passing native around components with Expect/Actual definition

Your expect/actual class "contract" is strong. You can't modify a class constructor in one platform, but not in the others.

You can mitigate that, either by scanning on a native module side, or by passing around `scope : Scope` as a regular injection. This allows to request directly Koin with some missing parts.

:::info
This kind of dynamic part is not backed by the annotations. Use this kind of behavior carefully. 
:::

In commonMain:
```kotlin
// commonMain

@Module
class NativeModuleD() {
    
    //dynamically passing current Koin scope to let resolve side components
    @Factory 
    fun providesPlatformComponentD(scope: Scope) : PlatformComponentD = PlatformComponentD(scope)
}

expect class PlatformComponentD(scope: Scope) {
    fun sayHello() : String
}
```

In native sourceSets:

```kotlin
// androidMain
actual class PlatformComponentD actual constructor(scope: Scope) {
    
    // use scope to retrieve context
    val context : Context = scope.get<Context>()
    actual fun sayHello() : String = "I'm Android - D - with $context"
}

// iOSMain
actual class PlatformComponentD actual constructor(scope: Scope) {
    actual fun sayHello() : String = "I'm iOS - D"
}
```