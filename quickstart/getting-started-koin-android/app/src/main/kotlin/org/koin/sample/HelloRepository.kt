package org.koin.sample

import org.koin.core.annotation.Single

/**
 * Repository to provide a "Hello" data
 */

interface HelloRepository {
    fun giveHello(): String
}

@Single
class HelloRepositoryImpl() : HelloRepository {
    override fun giveHello() = "Hello Koin"
}