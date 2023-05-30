package org.koin.example.animal

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.random.Random

interface Animal

@Single(binds = [])
class Dog : Animal

@Single(binds = [])
class Cat : Animal

@Module
@ComponentScan
class AnimalModule {

    @Factory
    fun animal(cat: Cat, dog: Dog): Animal = if (randomBoolean()) cat else dog

    private fun randomBoolean(): Boolean = Random.nextBoolean()
}