package org.koin.sample.androidx.app

import androidx.lifecycle.ViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.sample.android.library.AbstractRepository
import org.koin.sample.androidx.repository.Repository


@KoinViewModel
class CoffeeViewModel(val repository : AbstractRepository) : ViewModel()

@KoinViewModel
class TodoViewModel(val repository: Repository) : ViewModel()