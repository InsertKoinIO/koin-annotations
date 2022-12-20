package org.koin.sample.android.library

import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface Tester

@Single
@Named("fake_tester")
class FakeTester : Tester

@Single
@Named("other_tester")
class OtherTester : Tester