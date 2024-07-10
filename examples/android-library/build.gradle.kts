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
}

dependencies {
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.android.appcompat)
    ksp(libs.koin.ksp)
    implementation(project(":coffee-maker-module"))

    testImplementation(libs.koin.test)
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
//    arg("KOIN_DEFAULT_MODULE","false")
}