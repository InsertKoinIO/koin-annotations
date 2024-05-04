package org.koin.sample.androidx.app

import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.koin.sample.android.library.MyScope

@Scoped
@Scope(MyScope::class)
class ScopedStuff