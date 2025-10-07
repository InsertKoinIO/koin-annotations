package org.koin.compiler.type


internal val typeWhiteList = listOf(
    "kotlin.Any",
    "android.content.Context",
    "android.app.Application",
    "androidx.appcompat.app.AppCompatActivity",
    "androidx.activity.ComponentActivity",
    "android.app.Activity",
    "androidx.fragment.app.Fragment",
    "androidx.lifecycle.SavedStateHandle",
    "androidx.lifecycle.ViewModel",
    "androidx.work.WorkerParameters",
    "org.koin.ktor.plugin.RequestScope",
    "org.koin.core.scope.Scope"
)
internal val typeWhiteListActualExpect = typeWhiteList.flatMap { listOf("${it}_Actual","${it}_Expect") }
val fullWhiteList = typeWhiteList + typeWhiteListActualExpect