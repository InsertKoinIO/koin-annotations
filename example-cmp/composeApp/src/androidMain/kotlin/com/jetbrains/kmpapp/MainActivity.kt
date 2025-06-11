package com.jetbrains.kmpapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jetbrains.kmpapp.native.PlatformComponentD
import com.jetbrains.kmpapp.other.android.PlatformComponentC
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    val platformComponentC : PlatformComponentC by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }

        println("platformComponentC: ${platformComponentC.sayHello()}")

        println("platformComponentD: ${getKoin().get<PlatformComponentD>().sayHello()}")
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}