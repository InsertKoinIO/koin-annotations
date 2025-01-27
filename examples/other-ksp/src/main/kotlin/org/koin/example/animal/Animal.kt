package org.koin.example.animal

import org.koin.core.annotation.*
import kotlin.random.Random

public interface Animal

@Single(binds = [])
public class Dog : Animal

@Single(binds = [])
public class Cat : Animal

public class Bunny(public val color: String) : Animal

@Single
public class Farm(@WhiteBunny public val whiteBunny: Bunny, @BlackBunny public val blackBunny: Bunny)

@Named
public annotation class WhiteBunny

@Qualifier
public annotation class BlackBunny

@Module
@ComponentScan
public class AnimalModule {
    @Factory
    public fun animal(cat: Cat, dog: Dog): Animal = if (randomBoolean()) cat else dog

    @Single
    @WhiteBunny
    public fun whiteBunny(): Bunny = Bunny("White")

    @Single
    @BlackBunny
    public fun blackBunny(): Bunny = Bunny("Black")

    private fun randomBoolean(): Boolean = Random.nextBoolean()
}