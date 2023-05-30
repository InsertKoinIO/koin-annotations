val koinVersion: String by project
val koinKspVersion: String by project

plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
    idea
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
    implementation(kotlin("stdlib"))
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-annotations:$koinKspVersion")
    ksp("io.insert-koin:koin-ksp-compiler:$koinKspVersion")

    testImplementation("io.insert-koin:koin-test:$koinVersion")
}
