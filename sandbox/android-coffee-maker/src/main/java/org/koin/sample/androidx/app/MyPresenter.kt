package org.koin.sample.androidx.app

import org.koin.core.annotation.Factory
import org.koin.sample.androidx.MainActivity

@Factory
class MyPresenter(val mainActivity: MainActivity)