package org.koin.compiler.type


internal val typeWhiteList = listOf(
    "kotlin.Any",
    "android.content.Context",
    "android.app.Application",
    "androidx.appcompat.app.AppCompatActivity",
    "androidx.appcompat.app.AppCompatActivity",
    "androidx.fragment.app.Fragment",
    "androidx.lifecycle.SavedStateHandle",
    "androidx.lifecycle.ViewModel",
    "androidx.work.WorkerParameters",
    "org.koin.ktor.plugin.RequestScope"
)