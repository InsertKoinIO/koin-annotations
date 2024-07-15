plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

version = "1.0-SNAPSHOT"

tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java) {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-api=strict")
    }
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp)

    testImplementation(libs.koin.test)
    testImplementation(libs.junit)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}

