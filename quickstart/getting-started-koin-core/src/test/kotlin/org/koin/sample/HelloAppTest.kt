package org.koin.sample

import org.junit.Rule
import org.junit.Test
import org.koin.ksp.generated.*
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import kotlin.test.assertEquals

class HelloAppTest : KoinTest {

    val model by inject<MessageGenerator>()
    val service by inject<MessageService>()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(MessageModule().module)
    }

    @Test
    fun `unit test`() {
        val helloApp = MessageApplication()
        helloApp.displayMessage()

        assertEquals(service, helloApp.helloService)
        assert(service.hello().contains("Hey"))
    }
}