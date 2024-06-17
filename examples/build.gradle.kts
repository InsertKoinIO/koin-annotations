buildscript {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        jcenter()
    }
}

plugins {
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
}

repositories {
    google()
    mavenCentral()
    mavenLocal()
}

allprojects {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

