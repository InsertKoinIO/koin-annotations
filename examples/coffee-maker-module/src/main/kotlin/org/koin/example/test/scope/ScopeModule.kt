package org.koin.example.test.scope

import org.koin.core.annotation.*

class MyScope
class MyAnotherScope
interface AdditionalTypeScope
interface AdditionalTypeScope2
interface AdditionalTypeMultiScope
interface AdditionalTypeMultiScope2

const val MY_SCOPE_SESSION = "MY_SCOPE_SESSION"
const val MY_SCOPE_SESSION2 = "MY_SCOPE_SESSION2"

@Scope(MyScope::class)
class MyScopedComponent(val myScope: MyScope)
@Scope(MyScope::class)
@Scope(MyAnotherScope::class)
class MyScopedComponentMultiScope(@Provided val myScope: Lazy<MyScope>, @Provided val myAnotherScope: Lazy<MyAnotherScope>)

class MyScopedComponent2(val myScope: MyScope)

class MyScopedComponentMultiScope2(val myScope: Lazy<MyScope>, val myAnotherScope: Lazy<MyAnotherScope>)

@Scope(MyScope::class)
@Factory
class MyScopedComponent3(val myScope: MyScope) : AdditionalTypeScope

@Scope(MyScope::class)
@Scope(MyAnotherScope::class)
@Factory
class MyScopedComponentMultiScope3(@Provided val myScope: Lazy<MyScope>, @Provided val myAnotherScope: Lazy<MyAnotherScope>) : AdditionalTypeMultiScope

@Scope(MyScope::class)
@Scoped(binds = [AdditionalTypeScope2::class])
class MyScopedComponent5(val myScope: MyScope) : AdditionalTypeScope, AdditionalTypeScope2

@Scope(MyScope::class)
@Scope(MyAnotherScope::class)
@Scoped(binds = [AdditionalTypeMultiScope2::class])
class MyScopedComponentMultiScope5 : AdditionalTypeMultiScope, AdditionalTypeMultiScope2

class MyScopedComponent4(val myScope: MyScope)

class MyScopedComponentMultiScope4(val myScope: Lazy<MyScope>, val myAnotherScope: Lazy<MyAnotherScope>)

@Scope(name = MY_SCOPE_SESSION)
class MyScopedSessionComponent

@Scope(name = MY_SCOPE_SESSION)
@Scope(name = MY_SCOPE_SESSION2)
class MyScopedSessionComponentMultiScope

@Module
@ComponentScan
class ScopeModule {

    @Scope(MyScope::class)
    fun myScopedComponent2(myScope: MyScope) = MyScopedComponent2(myScope)

    @Scope(MyScope::class)
    @Scope(MyAnotherScope::class)
    fun myScopedComponentMultiScope2(@Provided myScope: Lazy<MyScope>, @Provided myAnotherScope: Lazy<MyAnotherScope>) = MyScopedComponentMultiScope2(myScope, myAnotherScope)

    @Scope(MyScope::class)
    @Factory
    fun myScopedComponent4(myScope: MyScope) = MyScopedComponent4(myScope)

    @Scope(MyScope::class)
    @Scope(MyAnotherScope::class)
    @Factory
    fun myScopedComponentMultiScope4(@Provided myScope: Lazy<MyScope>, @Provided myAnotherScope: Lazy<MyAnotherScope>) = MyScopedComponentMultiScope4(myScope, myAnotherScope)
}