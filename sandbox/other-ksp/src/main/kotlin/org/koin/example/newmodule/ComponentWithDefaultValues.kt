package org.koin.example.newmodule

import org.koin.core.annotation.Single

public interface ComponentInterface {

    public companion object Default : ComponentInterface
}

@Single
public class ComponentWithDefaultValues(
    private val dependency: ComponentInterface = ComponentInterface.Default
)