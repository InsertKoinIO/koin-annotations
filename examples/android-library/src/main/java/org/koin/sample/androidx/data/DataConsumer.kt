package org.koin.sample.androidx.data

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped
import java.util.UUID

@Factory
class DataConsumer

class MyDataConsumer(val dc : DataConsumer)

@Factory
fun funDataConsumer(dc : DataConsumer) = MyDataConsumer(dc)

class MyDataScope

@Scoped
@Scope(MyDataScope::class)
class ScopeData {
    val id : String = UUID.randomUUID().toString()
}