---
title: Annotations for Definitions and Modules in Kotlin Multiplatform App
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

## Declaring Common Modules

In your commonMain sourceSet, you just need to declare a Module to scan the package that will have native implementations of your expect class or function.

Below we have a `PlatformModule`, scanning in `com.jetbrains.kmpapp.platform` package where we have `PlatformHelper` expect class. The module class is annotated with `@Module` and `@ComponentScan` annotations. 

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

## Using Modules and Expect/actual for Kotlin Native Components

in a Kotlin Multiplatform application you will need to have specific implementation per platform on some components. You can share those components at definition level, with expect/actual on the given class. 
Or you can share an entire module, with expect/actual on the class module.

### Sharing Definitions - Scanning for expect definitions

From your commonMain code sourceSet, you can scan for expect classes that will have their own implementation on each native platform. Be aware to use `expect/actual` definitions, even on constructors.

In commonMain:
```kotlin
@Module
@ComponentScan("com.jetbrains.kmpapp.native")
class NativeModuleA()

// package com.jetbrains.kmpapp.native
@Factory
expect class PlatformComponentA() {
    fun sayHello() : String
}
```

in native sourceSets:

```kotlin
// androidMain

// package com.jetbrains.kmpapp.native
actual class PlatformComponentA actual constructor() {
    actual fun sayHello() : String = "I'm Android - A"
}

// iOSMain

// package com.jetbrains.kmpapp.native
actual class PlatformComponentA actual constructor() {
    actual fun sayHello() : String = "I'm iOS - A"
}
```

### Sharing Definitions - Module with expect definition function

From your commonMain code sourceSet, you can define an expect class definition with their own implementation on each native platform. Be aware to use `expect/actual` definitions, even on constructors.

In commonMain:
```kotlin
@Module
expect class NativeModuleB() {

    @Factory
    fun providesPlatformComponentB() : PlatformComponentB
}

// package com.jetbrains.kmpapp.native
expect class PlatformComponentB() {
    fun sayHello() : String
}
```

in native sourceSets:

```kotlin
// androidMain

// package com.jetbrains.kmpapp.native
actual class PlatformComponentB actual constructor() {
    actual fun sayHello() : String = "I'm Android - B"
}

// iOSMain

// package com.jetbrains.kmpapp.native
actual class PlatformComponentB actual constructor() {
    actual fun sayHello() : String = "I'm iOS - B"
}
```

### Sharing Module - expect/actual Module

In commonMain:
```kotlin
@Module
expect class NativeModuleC()
```

in native sourceSets:

```kotlin
// androidMain

@Module
@ComponentScan("com.jetbrains.kmpapp.other.android")
actual class NativeModuleC actual constructor()

// package com.jetbrains.kmpapp.other.android
@Factory 
class PlatformComponentC(val context: Context) {
    fun sayHello() : String = "I'm Android - C - $context"
}
```

:::note
Your module needs to not have any definition from the commonMain source set, to be considered as used only on the platform. 
:::