package org.koin.example.test.scope

import org.koin.core.annotation.*

class MyScope
interface AdditionalTypeScope
interface AdditionalTypeScope2

const val MY_SCOPE_SESSION = "MY_SCOPE_SESSION"

@Scope(MyScope::class)
class MyScopedComponent(val myScope: MyScope)

class MyScopedComponent2(val myScope: MyScope)

@Scope(MyScope::class)
@Factory
class MyScopedComponent3(val myScope: MyScope) : AdditionalTypeScope

@Scope(MyScope::class)
@Scoped(binds = [AdditionalTypeScope2::class])
class MyScopedComponent5(val myScope: MyScope) : AdditionalTypeScope, AdditionalTypeScope2

class MyScopedComponent4(val myScope: MyScope)

@Scope(name = MY_SCOPE_SESSION)
class MyScopedSessionComponent

@Module
@ComponentScan
class ScopeModule {

    @Scope(MyScope::class)
    fun myScopedComponent2(myScope: MyScope) = MyScopedComponent2(myScope)

    @Scope(MyScope::class)
    @Factory
    fun myScopedComponent4(myScope: MyScope) = MyScopedComponent4(myScope)
}