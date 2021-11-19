pluginManagement {

    val kotlinVersion: String by settings
    val kspVersion: String by settings

    plugins {
        id("com.google.devtools.ksp") version kspVersion
        kotlin("jvm") version kotlinVersion
        id("com.android.application")
        id("kotlin-android")
    }
    repositories {
        gradlePluginPortal()
        google()
        mavenLocal()
    }
}

rootProject.name = "playground"

include(":coffee-maker")
include(":coffee-maker-module")
include(":android-coffee-maker")
//include(":compile-perf")
