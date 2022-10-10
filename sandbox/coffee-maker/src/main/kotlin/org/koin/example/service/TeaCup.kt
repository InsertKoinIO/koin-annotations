package org.koin.example.service

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory(binds = [Cup::class])
@Named("TeaCup")
class TeaCup : Cup() {
    override fun setMyName(): String = "TeaCup"
}