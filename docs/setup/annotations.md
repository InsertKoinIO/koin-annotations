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

Latest KSP compatible version: `1.9.24-1.0.20`

## Kotlin & Multiplatform

### Grrovy

Here below how you can configure a Kotlin app:

```groovy
dependencies {
    // Koin
    implementation "io.insert-koin:koin-core:$koin_version"
    // Koin Annotations
    compile "io.insert-koin:koin-annotations:$koin_ksp_version"
    ksp "io.insert-koin:koin-ksp-compiler:$koin_ksp_version"
}
```

Check to add sourceSets config:

```groovy
// Use KSP Generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}


```

### Kotlin KTS

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

        // KSP Common sourceSet
        sourceSets.named("commonMain").configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
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

// KSP Metadata Trigger
project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if(name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

```

## Android App Setup

### Groovy

Here below how you can configure an Android app:

```groovy
// Use KSP Plugin
apply plugin: 'com.google.devtools.ksp'

// Use KSP Generated sources
android {
    applicationVariants.all { variant ->
        variant.sourceSets.java.each {
            it.srcDirs += "build/generated/ksp/${variant.name}/kotlin"
        }
    }
}

dependencies {
    // Koin for Android
    implementation "io.insert-koin:koin-android:$koin_version"
    implementation "io.insert-koin:koin-annotations:$koin_ksp_version"
    ksp "io.insert-koin:koin-ksp-compiler:$koin_ksp_version"
}
```

If you use several KSP libraries (like Room), you can use this way of declaring generated sources:

```groovy
libraryVariants.all { variant ->
  variant.addJavaSourceFoldersToModel(file("build/generated/ksp/${variant.name}/kotlin"))
}
```

### Kotlin KTS

- use KSP Gradle plugin
- add dependency for koin annotations and koin ksp compiler
- set sourceSet

```groovy
plugins {
   id("com.google.devtools.ksp")
}


android {

    dependencies {
        // Koin
        implementation("io.insert-koin:koin-android:$koin_version")
        // Koin Annotations
        implementation("io.insert-koin:koin-annotations:$koin_annotations_version")
        // Koin Annotations KSP Compiler
        ksp("io.insert-koin:koin-ksp-compiler:$koin_annotations_version")
    }

    // Set KSP sourceSet
    applicationVariants.all {
        val variantName = name
        sourceSets {
            getByName("main") {
                java.srcDir(File("build/generated/ksp/$variantName/kotlin"))
            }
        }
    }
}

```
