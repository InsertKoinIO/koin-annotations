pluginManagement {

    val kotlinVersion: String by settings
    val kspVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
        id("com.android.application")
        id("kotlin-android")
    }
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "playground"

include(":coffee-maker")
include(":coffee-maker-module")

include(":compile-perf")

include(":android-coffee-maker")
include(":android-library")