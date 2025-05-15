---
title: Koin Annotations
---

Setup Koin Annotations for your project 

## Version

You can find all Koin packages on [maven central](https://search.maven.org/search?q=io.insert-koin).

Here are the current available versions:

## Setup & Current Version

Here are the current available Koin projects versions:

| Project   |      Version      |
|----------|:-------------:|
| koin-annotations-bom |  [![Maven Central](https://img.shields.io/maven-central/v/io.insert-koin/koin-annotations-bom)](https://mvnrepository.com/artifact/io.insert-koin/koin-annotations-bom) |
| koin-annotations |  [![Maven Central](https://img.shields.io/maven-central/v/io.insert-koin/koin-annotations)](https://mvnrepository.com/artifact/io.insert-koin/koin-annotations) |
| koin-ksp-compiler |  [![Maven Central](https://img.shields.io/maven-central/v/io.insert-koin/koin-ksp-compiler)](https://mvnrepository.com/artifact/io.insert-koin/koin-ksp-compiler) |


## KSP Plugin

We need KSP Plugin to work (https://github.com/google/ksp). Follow the official (KSP Setup documentation)[https://kotlinlang.org/docs/ksp-quickstart.html]

Just add the Gradle plugin:
```groovy
plugins {
    id "com.google.devtools.ksp" version "$ksp_version"
}
```

Latest KSP compatible version: `2.0.21-1.0.28`

## Kotlin Multiplatform Setup

In a standard Kotlin/Kotlin Multiplatform project, you need to setup KSP as follow:

- use KSP Gradle plugin
- add dependency in commonMain for koin annotations
- set sourceSet for commonMain
- add KSP dependencies tasks with koin compiler
- setup compilation task dependency to `kspCommonMainKotlinMetadata`

```groovy
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
project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

```

## Android & Ktor App KSP Setup

- use KSP Gradle plugin
- add dependency for koin annotations and koin ksp compiler
- set sourceSet

```groovy
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
}
```
