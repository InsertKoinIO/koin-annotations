package org.koin.sample

import org.koin.core.annotation.Single

/**
 * Hello Service - interface
 */
interface MessageService {
    fun hello(): String
}


// service class with injected helloModel instance
/**
 * Hello Service Impl
 * Will use HelloMessageData data
 */
@Single
class MessageGeneratorImpl(private val generator: MessageGenerator) : MessageService {

    override fun hello() = "Hey, ${generator.newMessage()}"
}