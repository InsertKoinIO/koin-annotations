package org.koin.sample.androidx.app

import android.content.Context
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.sample.androidx.MainActivity

@Factory
class MyPresenter(@InjectedParam val mainActivity: MainActivity, @InjectedParam val context: Context)