package org.koin.example.service

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

/**
 * Coffee mugs to be injected in different ways
 *
 * They provide their unique name to base class @see [Cup]
 *
 * @author Alperen Babagil
 */

@Factory(binds = [Cup::class])
@Named(name = "CoffeeMugStringQualifierLazy")
class CoffeeMugStringQualifierLazy : Cup() {
    override fun setMyName(): String = "CoffeeMugStringQualifierLazy"
}

@Factory(binds = [Cup::class])
@Named(value = CoffeeMugTypeQualifierLazy::class)
class CoffeeMugTypeQualifierLazy : Cup() {
    override fun setMyName(): String = "CoffeeMugTypeQualifierLazy"
}

@Factory(binds = [Cup::class])
class CoffeeMugNoQualifierLazy : Cup() {
    override fun setMyName(): String = "CoffeeMugNoQualifierLazy"
}


@Factory(binds = [Cup::class])
@Named(name = "CoffeeMugStringQualifier")
class CoffeeMugStringQualifier : Cup() {
    override fun setMyName(): String = "CoffeeMugStringQualifier"
}

@Factory(binds = [Cup::class])
@Named(value = CoffeeMugTypeQualifier::class)
class CoffeeMugTypeQualifier : Cup() {
    override fun setMyName(): String = "CoffeeMugTypeQualifier"
}


