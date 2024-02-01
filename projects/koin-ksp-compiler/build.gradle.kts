import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":koin-annotations"))
            implementation(libs.koin.core)
        }
        jvmMain.dependencies {
            implementation(libs.ksp.api)
        }
    }
}

apply(from = file("../gradle/publish.gradle.kts"))
