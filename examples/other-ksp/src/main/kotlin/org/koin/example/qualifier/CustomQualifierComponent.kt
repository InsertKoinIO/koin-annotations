package org.koin.example.qualifier

import jakarta.inject.Qualifier
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val niaDispatcher: NiaDispatchers)

enum class NiaDispatchers {
    Default,
    IO,
}

data class CoroutineDispatcher(val name : String)
class CoroutineDispatcherConsumer(val dispatcher : CoroutineDispatcher)

@Module
@Configuration
class DispatchersModule {

    @Single
    @Dispatcher(NiaDispatchers.IO)
    fun providesIODispatcher(): CoroutineDispatcher = CoroutineDispatcher("IO")

    @Single
    @Dispatcher(NiaDispatchers.Default)
    fun providesDefaultDispatcher(): CoroutineDispatcher = CoroutineDispatcher("Default")

    @Single
    fun coroutineDispatcherConsumer(@Dispatcher(NiaDispatchers.IO) dispatcher: CoroutineDispatcher) = CoroutineDispatcherConsumer(dispatcher)
}