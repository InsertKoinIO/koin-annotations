package org.koin.sample.androidx.app.scope

import android.app.Activity
import androidx.lifecycle.ViewModel
import org.koin.android.annotation.ActivityScope
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.ViewModelScope
import java.util.UUID

@ViewModelScope
class ScopedData {
    val id = UUID.randomUUID().toString()
}

@ViewModelScope
class ScopedOtherData {
    val id = UUID.randomUUID().toString()
}

@ViewModelScope
class SecondOtherData(val sd : ScopedData) {
    val id = sd.id
}

@KoinViewModel
class ScopeViewModel(val sd : ScopedData, val sod : ScopedOtherData, val ssd : SecondOtherData) : ViewModel()

@ActivityScope
class MyActivityScope {
    val id = UUID.randomUUID().toString()
}

class MyActivityInjectedScope(activity: Activity) {
    val id = activity.hashCode()
}

@ActivityScope
class MyActivityOtherScope(val mas : MyActivityScope) {
    val id = mas.id
}