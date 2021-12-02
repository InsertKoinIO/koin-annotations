---
title: Android
---

> This tutorial lets you write an Android/Kotlin application and use Koin inject and retrieve your components.

## Gradle Setup

Add KSP in your root Gradle config:

```groovy
// Add KSP Plugin
plugins {
    id "com.google.devtools.ksp" version "$ksp_version"
}
```

Add the Koin Android dependency like below:

```groovy
// Add Maven Central to your repositories if needed
repositories {
	mavenCentral()    
}

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

## Basic Components

Let's create a HelloRepository to provide some data:

```kotlin
interface HelloRepository {
    fun giveHello(): String
}

@Single
class HelloRepositoryImpl() : HelloRepository {
    override fun giveHello() = "Hello Koin"
}
```

We tag it with `@Single` to declare it as single instance.

Let's create a presenter class, for consuming this data:

```kotlin

@Factory
class MySimplePresenter(val repo: HelloRepository) {

    fun sayHello() = "${repo.giveHello()} from $this"
}
```

We tag it as `@Factory`, to declare it as Koin factory instance (recreated each time you need)

### ViewModel

Let's create a ViewModel class, for consuming this data:

```kotlin
@KoinViewModel
class MyViewModel(val repo : HelloRepository) : ViewModel() {

    fun sayHello() = "${repo.giveHello()} from $this"
}
```

We tag it `@KoinViewModel` to declare it as Koin ViewModel instance.


## Writing the Koin module

Let's create a `AppModule` class to scan our components:

```kotlin
@Module
@ComponentScan("org.koin.sample")
class AppModule
```

* `@Module` - tag the class a Module
* `@ComponentScan("org.koin.sample")` - scan given package for Koin definitions

## Start Koin

Now that we have a module, let's start it with Koin. Open your application class, or make one (don't forget to declare it in your manifest.xml). Just call the `startKoin()` function:

```kotlin
// generated from Koin KSP
import org.koin.ksp.generated.module

class MyApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        // Start Koin
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(AppModule().module)
        }
    }
}
```

## Injecting dependencies

The `MySimplePresenter` component will be created with `HelloRepository` instance. To get it into our Activity, let's inject it with the `by inject()` delegate injector: 

```kotlin
class MySimpleActivity : AppCompatActivity() {

    // Lazy injected MySimplePresenter
    val firstPresenter: MySimplePresenter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        //...
    }
}
```

> The `by inject()` function allows us to retrieve Koin instances, in Android components runtime (Activity, fragment, Service...)

> The `get()` function is here to retrieve directly an instance (non lazy)

The `MyViewModel` component will be created with `HelloRepository` instance. To get it into our Activity, let's inject it with the `by viewModel()` delegate injector:

```kotlin
class MyViewModelActivity : AppCompatActivity() {
    
    // Lazy Inject ViewModel
    val myViewModel: MyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        //...
    }
}
```

:::info
>The `by viewModel()` function allows us to retrieve a ViewModel instance from Koin, linked to the Android ViewModelFactory.

> The `getViewModel()` function is here to retrieve directly an instance (non lazy)
:::