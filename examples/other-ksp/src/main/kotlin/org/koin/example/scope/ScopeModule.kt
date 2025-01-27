package org.koin.example.scope

import org.koin.core.annotation.*
import org.koin.example.MyOtherComponent

@Scope(name = "my_scope")
public class MyScope

@Scope(name = "my_scope")
@Scoped
public class MyScopedInstance

@Scope(name = "my_scope")
@Factory
public class MyScopeFactory(
    public val oc : MyOtherComponent,
    @ScopeId(name = "my_scope_id")
    public val msi : MyScopedInstance
)

@Module
@ComponentScan
public class ScopeModule