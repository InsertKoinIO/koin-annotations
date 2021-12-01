package org.koin.sample

import org.koin.core.annotation.Single
import kotlin.random.Random

/**
 * A class to hold our message data
 */
@Single
class MessageGenerator {
    fun newMessage(): String {
        val id = Random.nextLong()
        return "Hello Koin! - id:'$id'"
    }
}