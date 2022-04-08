package org.koin.sample.androidx.app

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.example.coffee.CoffeeMaker
import org.koin.sample.androidx.repository.Repository


@KoinViewModel
class CoffeeViewModel(val coffeeMaker : CoffeeMaker) : ViewModel()

@KoinViewModel
class TodoViewModel(val repository: Repository) : ViewModel()