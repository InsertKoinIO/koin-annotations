package org.koin.sample.androidx.app.scope

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import org.koin.core.annotation.ViewModelScope
import org.koin.sample.androidx.MainActivity
import java.util.UUID

@ViewModelScope
class ScopedData {
    val id = UUID.randomUUID().toString()
}

@ViewModelScope
class ScopedOtherData {
    val id = UUID.randomUUID().toString()
}


@KoinViewModel
class ScopeViewModel(val sd : ScopedData, val sod : ScopedOtherData) : ViewModel()