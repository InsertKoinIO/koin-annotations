plugins {
    alias(libs.plugins.ksp)
    kotlin("jvm")
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

dependencies {
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    ksp(libs.koin.ksp)
    implementation(project(":coffee-maker-module"))

    testImplementation(libs.koin.test)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}
