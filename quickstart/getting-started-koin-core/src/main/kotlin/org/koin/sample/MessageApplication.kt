package org.koin.sample

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
// For Koin Generation
import org.koin.ksp.generated.*

/**
 * HelloApplication - Application Class
 * use HelloService
 */
class MessageApplication : KoinComponent {

    // Inject HelloService
    val helloService: MessageService by inject()

    // display our data
    fun displayMessage() = println(helloService.hello())
}

/**
 * run app from here
 */
fun main() {
    startKoin {
        modules(MessageModule().module)
    }
    MessageApplication().displayMessage()
}