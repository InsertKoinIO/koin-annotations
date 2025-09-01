---
title: Koin Annotations
---

Setup Koin Annotations for your project 

## Current Versions

You can find all Koin packages on [maven central](https://search.maven.org/search?q=io.insert-koin).

Here are the currently available Koin Annotations versions:

- **Stable**: [![Maven Central](https://img.shields.io/maven-central/v/io.insert-koin/koin-annotations/2.1.0)](https://mvnrepository.com/artifact/io.insert-koin/koin-annotations) - Use for production applications
- **Beta/RC**: [![Maven Central](https://img.shields.io/maven-central/v/io.insert-koin/koin-annotations/2.2.0)](https://mvnrepository.com/artifact/io.insert-koin/koin-annotations) - Preview of upcoming features

## KSP Plugin

We need the KSP Plugin to work (https://github.com/google/ksp). Follow the official [KSP Setup documentation](https://kotlinlang.org/docs/ksp-quickstart.html).

Just add the Gradle plugin:
```kotlin
plugins {
    id("com.google.devtools.ksp") version "$ksp_version"
}
```

**KSP Compatibility**: Latest Koin/KSP compatible version is `2.1.21-2.0.2` (KSP2)

:::info
KSP version format: `[Kotlin version]-[KSP version]`. Make sure your KSP version is compatible with your Kotlin version.
:::

## Android & Ktor App KSP Setup

- use KSP Gradle plugin
- add dependency for koin annotations and koin ksp compiler
- set sourceSet

```kotlin
plugins {
    id("com.google.devtools.ksp") version "$ksp_version"
}

dependencies {
    // Koin
    implementation("io.insert-koin:koin-android:$koin_version")
    // Koin Annotations
    implementation("io.insert-koin:koin-annotations:$koin_annotations_version")
    // Koin Annotations KSP Compiler
    ksp("io.insert-koin:koin-ksp-compiler:$koin_annotations_version")
}
```

## Kotlin Multiplatform Setup

In a standard Kotlin/Kotlin Multiplatform project, you need to setup KSP as follow:

- use KSP Gradle plugin
- add dependency in commonMain for koin annotations
- set sourceSet for commonMain
- add KSP dependencies tasks with koin compiler
- setup compilation task dependency to `kspCommonMainKotlinMetadata`

```kotlin
plugins {
    id("com.google.devtools.ksp")
}

kotlin {

    sourceSets {
        
        // Add Koin Annotations
        commonMain.dependencies {
            // Koin
            implementation("io.insert-koin:koin-core:$koin_version")
            // Koin Annotations
            api("io.insert-koin:koin-annotations:$koin_annotations_version")
        }
    }
    
    // KSP Common sourceSet
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }       
}

// KSP Tasks
dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}

// Trigger Common Metadata Generation from Native tasks
tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}
```

