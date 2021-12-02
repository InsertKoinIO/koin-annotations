package org.koin.sample.view.viewmodel

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.sample.HelloRepository

@KoinViewModel
class MyViewModel(val repo: HelloRepository) : ViewModel() {

    fun sayHello() = "${repo.giveHello()} from MyViewModel"
}