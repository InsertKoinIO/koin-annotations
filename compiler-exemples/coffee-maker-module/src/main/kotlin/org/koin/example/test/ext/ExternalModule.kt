package org.koin.example.test.ext

import org.koin.core.annotation.*


class TestComponent
class TestComponentConsumer(val tc: TestComponent, val id: String)

@Single
class TestComponentConsumer2(@Named("tc") val tc: TestComponent, @InjectedParam val id: String)

@Factory
class TestComponentConsumer3(val tc: TestComponent?, @InjectedParam val id: String?)

@Single
class PropertyComponent(@Property("prop_id") val id: String)

class PropertyComponent2(val id: String)

@Module
@ComponentScan
class ExternalModule {

    @Single(createdAtStart = true)
    @Named("tc")
    fun testComponent() = TestComponent()

    @Singleton
    fun testComponentConsumer(@Named("tc") tc: TestComponent, @InjectedParam id: String) = TestComponentConsumer(tc, id)

    @Single
    fun propertyComponent2(@Property("prop_id") id: String) = PropertyComponent2(id)
}