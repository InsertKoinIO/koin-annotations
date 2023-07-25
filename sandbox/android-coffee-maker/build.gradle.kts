val koinVersion: String by project
val koinAndroidVersion: String by project
val appcompatVersion : String by project
val koinKspVersion: String by project

plugins {
    id("com.android.application")
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
        applicationId = "org.gradle.kotlin.dsl.samples.androidstudio"
        targetSdkVersion(33)
        minSdkVersion(21)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    // to use KSP generated Code
    applicationVariants.all {
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

    implementation(project(":android-library"))

    implementation ("androidx.appcompat:appcompat:$appcompatVersion")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation("junit:junit:4.13.2")
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}
