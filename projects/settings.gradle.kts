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
    // Core
    ":koin-annotations",
    ":koin-ksp-compiler",
    ":koin-annotations-bom",
    ":koin-lazy-annotations",
    ":koin-lazy-ksp-compiler",
)