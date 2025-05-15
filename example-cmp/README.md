# Kotlin Multiplatform app template

[![official project](http://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This is a basic Kotlin Multiplatform app template for Android and iOS. It includes shared business logic and data handling, and a shared UI implementation using Compose Multiplatform.

> The template is also available [with native UI written in Jetpack Compose and SwiftUI](https://github.com/kotlin/KMP-App-Template-Native).
>
> The [`amper` branch](https://github.com/Kotlin/KMP-App-Template/tree/amper) showcases the same project configured with [Amper](https://github.com/JetBrains/amper).

![Screenshots of the app](images/screenshots.png)

### Technologies

The data displayed by the app is from [The Metropolitan Museum of Art Collection API](https://metmuseum.github.io/).

The app uses the following multiplatform dependencies in its implementation:

- [Compose Multiplatform](https://jb.gg/compose) for UI
- [Ktor](https://ktor.io/) for networking
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) for JSON handling
- [Kamel](https://github.com/Kamel-Media/Kamel) for image loading
- [Koin](https://github.com/InsertKoinIO/koin) for dependency injection

> These are just some of the possible libraries to use for these tasks with Kotlin Multiplatform, and their usage here isn't a strong recommendation for these specific libraries over the available alternatives. You can find a wide variety of curated multiplatform libraries in the [kmp-awesome](https://github.com/terrakok/kmp-awesome) repository.


# Using Koin Annotations

## KSP Setup

To setup Koin Annotations & KSP you need the following:

Gradle Toml file update: 
- [KSP](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/gradle/libs.versions.toml#L13)
- [Koin Annotations](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/gradle/libs.versions.toml#L9)

Compose App Build Gradle update:
- [KSP Plugin](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/build.gradle.kts#L8)
```kotlin
alias(libs.plugins.ksp)
```
- [Koin Annotations Dependency](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/build.gradle.kts#L56)
```kotlin
api(libs.koin.annotations)
```

Extra KSP Configurations:
- [KSP Common Source Sets](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/build.gradle.kts#L62)
```kotlin
sourceSets.named("commonMain").configure {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}
```
- [KSP Tasks](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/build.gradle.kts#L68)
```kotlin
dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}
```  
- [KSP Common Task Trigger](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/build.gradle.kts#L77)
```kotlin
project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
```

## Organize Modules & Components

All Koin configuration is available here: [Koin.kt](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/commonMain/kotlin/com/jetbrains/kmpapp/di/Koin.kt#L26)

Inside this configuration file:
- All definitions are available as module classes, annotated with `@Module`
- The `@ComponentScan` allows scanning annotated components for a given package (across Gradle modules)
- Class components are tagged with `@Single` or `@KoinViewModel`
- Some components are defined inside annotated Kotlin functions (Json & Http Client)

## Sharing Multiplatform Native Components

The [`NativeModule`](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/commonMain/kotlin/com/jetbrains/kmpapp/di/Koin.kt#L48) class allow to share native components with expect/actual mechanism. The idea is to inject a native implementation of `PlatformComponent` class.
Here is how it's organized:

In common Kotlin sourceSet:
- [NativeModule class](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/commonMain/kotlin/com/jetbrains/kmpapp/di/Koin.kt#L48)
```kotlin
@Module
expect class NativeModule
```
- [Expect PlatformComponent class](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/commonMain/kotlin/com/jetbrains/kmpapp/native/PlatformComponent.kt#L3C14-L3C31)
```kotlin
expect class PlatformComponent {
    fun sayHello() : String
}
```

In Android sourceSet: 
- [NativeModule class](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/androidMain/kotlin/com/jetbrains/kmpapp/di/Koin.android.kt)
```kotlin
@Module
@ComponentScan("com.jetbrains.kmpapp.native")
actual class NativeModule
```
- [Expect PlatformComponent class](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/androidMain/kotlin/com/jetbrains/kmpapp/native/PlatformComponent.android.kt)
```kotlin
@Single
actual class PlatformComponent(val context: Context){
    actual fun sayHello() : String = "I'm Android - $context"
}
```

In iOS sourceSet: 
- [NativeModule class](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/iosMain/kotlin/com/jetbrains/kmpapp/di/Koin.ios.kt)
```kotlin
@Module
@ComponentScan("com.jetbrains.kmpapp.native")
actual class NativeModule
```
- [Expect PlatformComponent class](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/iosMain/kotlin/com/jetbrains/kmpapp/native/PlatformComponent.ios.kt)
```kotlin
@Single
actual class PlatformComponent{
    actual fun sayHello() : String = "I'm iOS"
}
```

## Injecting dynamic parameters with @InjectedParam

Let's create an Id generator class that will receive some a prefix, to generate ids:

```kotlin
@Factory
class IdGenerator(@InjectedParam private val prefix : String) {
    
    fun generate() : String = prefix + KoinPlatformTools.generateId()
}
```

We are using `@InjectedParam` to indicates that `prefix` property will be dynamically injected via a call with `parametersOf`.

Later in our code, lets call our generator:

```kotlin
val idGen = KoinPlatform.getKoin().get<IdGenerator> { parametersOf("_prefix_") }.generate()
println("Id => $idGen")
```

It should produces something like `_prefix_d1a7ac22-0aee-4f3c-9d5c-41e6bfae5676`


## Customize Koin Configuration

To allow the use of Koin in multiplatform style, but allow some special configuration (like injecting Android context), we can allow this kind of [startup function](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/commonMain/kotlin/com/jetbrains/kmpapp/di/Koin.kt#L50):

```kotlin
// config allow to extend Koin configuration from caller side
fun initKoin(config : KoinAppDeclaration ?= null) {
    startKoin {
        modules(
            AppModule().module,
        )
        // call for any extra configuration
        config?.invoke(this)
    }
}
```

On [Android startup](https://github.com/InsertKoinIO/KMP-App-Template/blob/converted_koin_annotations/composeApp/src/androidMain/kotlin/com/jetbrains/kmpapp/MuseumApp.kt#L10):
```kotlin
class MuseumApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MuseumApp)
        }
    }
}
```
