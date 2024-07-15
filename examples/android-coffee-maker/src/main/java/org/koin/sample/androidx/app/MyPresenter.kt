package org.koin.sample.androidx.app

import android.content.Context
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Provided
import org.koin.sample.androidx.MainActivity
import org.koin.sample.androidx.data.ProvidedComponent

@Factory
class MyPresenter(@InjectedParam val mainActivity: MainActivity, val context: Context)