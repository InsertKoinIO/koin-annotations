plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.ksp)
}

val androidCompileSDK : String by project
val androidMinSDK : String by project

android {
    compileSdk = androidCompileSDK.toInt()
    defaultConfig {
        namespace = "org.koin.sample.android.library"
        minSdk = androidMinSDK.toInt()
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.android.appcompat)
    ksp(libs.koin.ksp)
    implementation(project(":coffee-maker-module"))
    api(libs.ktor.core)
    implementation(libs.ktor.cio)
    testImplementation(libs.koin.test)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
    arg("KOIN_DEFAULT_MODULE","false")
}