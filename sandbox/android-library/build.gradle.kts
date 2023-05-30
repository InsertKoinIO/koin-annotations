val koinVersion: String by project
val koinAndroidVersion: String by project
val appcompatVersion : String by project
val koinKspVersion: String by project

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    idea
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

android {
    compileSdkVersion(33)
    defaultConfig {
        targetSdkVersion(33)
        minSdkVersion(21)
    }
    // to use KSP generated Code
    libraryVariants.all {
        val variantName = name
        sourceSets {
            getByName("main") {
                java.srcDir(File("build/generated/ksp/$variantName/kotlin"))
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.insert-koin:koin-android:$koinAndroidVersion")

    implementation("io.insert-koin:koin-annotations:$koinKspVersion")
    ksp("io.insert-koin:koin-ksp-compiler:$koinKspVersion")

    implementation ("androidx.appcompat:appcompat:$appcompatVersion")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation("junit:junit:4.13.2")
}
