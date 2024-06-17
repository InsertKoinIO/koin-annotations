enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":coffee-maker",
    ":coffee-maker-module",
    ":other-ksp",
    ":compile-perf",
    ":android-coffee-maker",
    ":android-library"
)