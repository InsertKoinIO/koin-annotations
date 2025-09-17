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
    implementation(libs.koin.jsr330)
    ksp(libs.koin.ksp)
    implementation(project(":coffee-maker-module"))

    testImplementation(libs.koin.test)
    testImplementation(libs.junit)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
    arg("KOIN_LOG_TIMES","true")
}
