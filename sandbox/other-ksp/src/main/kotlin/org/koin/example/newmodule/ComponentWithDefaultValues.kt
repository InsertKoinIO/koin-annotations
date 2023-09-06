package org.koin.example.newmodule

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single

public interface ComponentInterface {

    public companion object Default : ComponentInterface
}

@Single
public class ComponentWithDefaultValues(
    private val dependency: ComponentInterface = ComponentInterface.Default
)

@Factory
public class ComponentWithProps(
    @Property("id") public val id : String = DEFAULT_ID
){
    public companion object {
        public const val DEFAULT_ID : String = "_empty_id"
    }
}