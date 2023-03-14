package org.koin.example.newmodule

import org.koin.core.annotation.Single

interface ComponentInterface {

    companion object Default : ComponentInterface
}

@Single
class ComponentWithDefaultValues(
    private val dependency: ComponentInterface = ComponentInterface.Default
)