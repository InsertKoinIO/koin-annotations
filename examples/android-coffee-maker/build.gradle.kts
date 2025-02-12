plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
}

val androidCompileSDK : String by project
val androidMinSDK : String by project

android {
    compileSdk = androidCompileSDK.toInt()
    defaultConfig {
        namespace = "org.koin.sample.androidx"
        minSdk = androidMinSDK.toInt()
        applicationId = "org.gradle.kotlin.dsl.samples.androidstudio"
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
    implementation(project(":android-library"))
    implementation(project(":coffee-maker-module"))

    testImplementation(libs.koin.test)
    testImplementation(libs.junit)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
    arg("KOIN_DEFAULT_MODULE","true")
    arg("KOIN_LOG_TIMES","true")
}