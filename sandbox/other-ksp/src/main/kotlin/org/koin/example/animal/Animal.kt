package org.koin.example.animal

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.random.Random

public interface Animal

@Single(binds = [])
public class Dog : Animal

@Single(binds = [])
public class Cat : Animal

@Module
@ComponentScan
public class AnimalModule {

    @Factory
    public fun animal(cat: Cat, dog: Dog): Animal = if (randomBoolean()) cat else dog

    private fun randomBoolean(): Boolean = Random.nextBoolean()
}