package org.koin.sample.androidx.app

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import org.koin.sample.android.library.Tester

@Factory
class AndroidCoffeeMakerTester(@Named("fake_tester") val tester: Tester)