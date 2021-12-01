package org.koin.sample.androidx

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.example.coffee.CoffeeMaker

@Module
@ComponentScan
class MyModule

@KoinViewModel
class CoffeeViewModel(val coffeeMaker : CoffeeMaker) : ViewModel()

@KoinViewModel
class TodoViewModel(val repository: Repository) : ViewModel()