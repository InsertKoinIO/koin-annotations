package org.koin.example.qualifier

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

public class LazyStuffCounter(public val lazyCounter : Lazy<StuffCounter>)
public data class StuffList(public val list : List<Stuff>)
public class StuffCounter(public val list: List<Stuff>, public val name : String = "")
public data class Stuff(val name : String)

@Module
@Configuration
public class QualifierModule {

    //Can't have Qualifier + List

    @Named("stuffs")
    @Single
    public fun listOfStuff() : StuffList = StuffList(listOf(Stuff("1"),Stuff("2")))

    @Single
    public fun counter(@Named("stuffs") stuffs : StuffList): StuffCounter = StuffCounter(stuffs.list)

    @Single
    @Named("lazyStuffCounter")
    public fun counter2(@Named("stuffs") stuffs : StuffList): StuffCounter = StuffCounter(stuffs.list,"lazy")

//    @Single
//    public fun lazyCounter(@Named("lazyStuffCounter") counter : StuffCounter) : LazyStuffCounter = LazyStuffCounter(lazy { counter })
     @Single
     public fun lazyCounter(@Named("lazyStuffCounter") counter : Lazy<StuffCounter>) : LazyStuffCounter = LazyStuffCounter(counter)

    @Named("another-counter")
    @Single
    public fun anotherList(): StuffList = StuffList(listOf(Stuff("1"),Stuff("2")))

    @Single
    public fun anotherCounter(@Named("another-counter") stuffs: StuffList): StuffCounter = StuffCounter(stuffs.list,"another-counter")
}