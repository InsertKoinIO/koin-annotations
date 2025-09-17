package org.koin.example.supertype

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
public class SuperTypesModule

public interface A

public open class B : A

public interface D

@Single
public class C : B(),D


class MyType {

    @Single
    class MyChildType
}
