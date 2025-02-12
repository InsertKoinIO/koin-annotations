package org.koin.sample.androidx.app.scope

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.koin.sample.androidx.MainActivity
import java.util.UUID

@Scoped
@Scope(MainActivity::class)
class ScopedData {
    val id = UUID.randomUUID().toString()
}

@KoinViewModel
@Scope(MainActivity::class)
class ScopeViewModel(val data : ScopedData) : ViewModel()