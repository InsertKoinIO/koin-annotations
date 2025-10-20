package com.jetbrains.kmpapp.screens.detail

import androidx.lifecycle.ViewModel
import com.jetbrains.kmpapp.data.MuseumObject
import com.jetbrains.kmpapp.data.MuseumRepository
import kotlinx.coroutines.flow.Flow
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Monitor

//@Monitor
@KoinViewModel
class DetailViewModel(private val museumRepository: MuseumRepository) : ViewModel() {

    fun getObject(objectId: Int): Flow<MuseumObject?> = museumRepository.getObjectById(objectId)

}
