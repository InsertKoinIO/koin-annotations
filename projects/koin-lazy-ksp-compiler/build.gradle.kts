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
            api(project(":koin-lazy-annotations"))
            api(libs.koin.core)
            api(libs.koin.coroutines)
        }
        jvmMain.dependencies {
            implementation(libs.ksp.api)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xcontext-receivers")
    }
}

apply(from = file("../gradle/publish.gradle.kts"))
