---
title: Kotlin - Annotations
---

> This tutorial lets you write a Kotlin application and use Koin inject and retrieve your components.

## Get the code

:::info
[The source code is available at on Github](https://github.com/InsertKoinIO/koin-annotations/tree/main/quickstart/quickstart-kotlin-annotations)
:::

## Setup

Add KSP in your root Gradle config:

```groovy
// Add KSP Plugin
plugins {
    id "com.google.devtools.ksp" version "$ksp_version"
}
```


Check that the dependencies are added like below:

```groovy
// Add Maven Central to your repositories if needed
repositories {
	mavenCentral()    
}

// Use KSP Plugin
apply plugin: 'com.google.devtools.ksp'

// Use KSP Generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

dependencies {
    // Koin
    compile "io.insert-koin:koin-core:$koin_version"
    compile "io.insert-koin:koin-annotations:$koin_ksp_version"
    ksp "io.insert-koin:koin-ksp-compiler:$koin_ksp_version"
}
```

## The application

In our small app we need to have components, to make coffee:

* CoffeeApplication - run the coffee maker
* CoffeeMaker - run the coffee process, by using a Pump and a Heater
* ElectricHeater - a electric heater, to heat the water of the coffee
* Thermosiphon - a pump to pump the water when it's hot


### Coffee Maker parts

Below are the components, declared as single instance with `@Single` annotation:

```kotlin
@Single
class ElectricHeater : Heater
```

```kotlin
@Single
class Thermosiphon(private val heater: Heater) : Pump 
```

```kotlin
@Single
class CoffeeMaker(private val pump: Pump, private val heater: Heater)
```


## The CoffeeApplication class

To "make our coffee", we need to create a runtime component. Let's write a `CoffeeApplication` class and tag it with `KoinComponent` interface. This will later allows us to use the `by inject()` functions to retrieve our component:

```kotlin
class CoffeeApplication : KoinComponent {
    
    // retrieve CoffeeMaker
    private val maker: CoffeeMaker by inject()

    fun run(){
        maker.brew()
    }
}
```

## Declaring a Module

We just need a module to scan all our components for the given package:

```kotlin
@Module
@ComponentScan("org.koin.sample.coffee")
class CoffeeAppModule
```

* `@Module` annotation declare the module class
* `@ComponentScan` annotation scan for components

## That's it!

Just start our app from a `main` function:

```kotlin
fun main(vararg args: String) {
    
    startKoin {
        // load our module
        modules(CoffeeAppModule().module)
    }
    
    // run coffee
    CoffeeApplication().run()
}

```
