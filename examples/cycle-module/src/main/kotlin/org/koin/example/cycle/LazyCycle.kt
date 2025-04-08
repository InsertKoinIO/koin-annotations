package org.koin.example.cycle

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

public class LazyCycleA(public val b : LazyCycleB)
public class LazyCycleB(public val a : LazyCycleA)


@Module
public class LazyCycleModule {

    @Single
    public fun providesLazyCycleA(b : LazyCycleB): LazyCycleA = LazyCycleA(b)

    @Single
    public fun providesLazyCycleB(a : LazyCycleA): LazyCycleB = LazyCycleB(a)
}