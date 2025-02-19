enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

include(
    ":coffee-maker",
    ":coffee-maker-glob",
    ":coffee-maker-module",
    ":other-ksp",
    ":compile-perf",
    ":android-coffee-maker",
    ":android-library",
    ":android-library-other",
)