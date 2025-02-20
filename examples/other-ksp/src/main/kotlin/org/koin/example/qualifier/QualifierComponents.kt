package org.koin.example.qualifier

import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

public class LazyStuffCounter(public val lazyCounter : Lazy<StuffCounter>)
public class StuffCounter(public val list: List<Stuff>, public val name : String = "")
public data class Stuff(val name : String)

@Module
public class QualifierModule {

    @Named("stuffs")
    @Single
    public fun listOfStuff() : List<Stuff> = listOf(Stuff("1"),Stuff("2"))

    @Single
    public fun counter(@Named("stuffs") stuffs : List<Stuff>): StuffCounter = StuffCounter(stuffs)

    @Single
    @Named("lazyStuffCounter")
    public fun counter2(@Named("stuffs") stuffs : List<Stuff>): StuffCounter = StuffCounter(stuffs,"lazy")

    @Single
    public fun lazyCounter(@Named("lazyStuffCounter") counter : StuffCounter) : LazyStuffCounter = LazyStuffCounter(lazy { counter })
    // @Single
    // public fun lazyCounter(@Named("lazyStuffCounter") counter : Lazy<StuffCounter>) : LazyStuffCounter = LazyStuffCounter(counter)
}