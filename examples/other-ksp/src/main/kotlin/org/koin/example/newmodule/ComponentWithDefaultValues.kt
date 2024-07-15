package org.koin.example.newmodule

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Property
import org.koin.core.annotation.PropertyValue
import org.koin.core.annotation.Single
import org.koin.example.newmodule.ComponentWithProps.Companion.DEFAULT_ID

public interface ComponentInterface {

    public companion object Default : ComponentInterface
}

@Single
public class ComponentWithDefaultValues(
    private val dependency: ComponentInterface = ComponentInterface.Default
)

@Factory
public class ComponentWithProps(
    @Property("id") public val id : String
){
    public companion object {
        @PropertyValue("id")
        public const val DEFAULT_ID : String = "_empty_id"
    }
}