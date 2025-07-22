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
    // Core
    ":koin-jsr330",
    ":koin-annotations",
    ":koin-ksp-compiler",
    ":koin-annotations-bom",
)