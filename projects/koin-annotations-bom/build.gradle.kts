import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api(project(":koin-annotations"))
        api(project(":koin-ksp-compiler"))
    }
}

apply(from = file("../gradle/publish-pom.gradle.kts"))
