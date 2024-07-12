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

## Declaring Common Modules & KMP Expect Components

In your commonMain sourceSet, you just need to declare a Module to scan the package that will have native implementations of your expect class or function.

Below we have a `PlatformModule`, scanning in `com.jetbrains.kmpapp.platform` package where we have `PlatformHelper` expect class. The module class is annotated with `@Module` and `@ComponentScan` annotations. 

```kotlin
// in commonMain

@Module
@ComponentScan("com.jetbrains.kmpapp.platform")
class PlatformModule

// package com.jetbrains.kmpapp.platform 

expect class PlatformHelper {
    fun getName() : String
}
```

:::note
The generated code is done in each platform implementation. Module package scanning will gather the right platform implementation.
:::

## Annotate Native Components

In each implementation sourceSet, you can now define the right platform implementation. Those implementation are annotated with `@Single` (could be anotehr definition annotation):

```kotlin
// in androidMain
// package com.jetbrains.kmpapp.platform

@Single
actual class PlatformHelper(
    val context: Context
){
    actual fun getName(): String = "I'm Android - $context"
}

// in nativeMain
// package com.jetbrains.kmpapp.platform

@Single
actual class PlatformHelper(){
    actual fun getName(): String = "I'm Native"
}
```