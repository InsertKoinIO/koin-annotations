package com.jetbrains.kmpapp.nativetest



actual class NetworkConfigurationModifier {

    /**
     * Modifies the network configuration based on the platform-specific implementation.
     *
     * The actual implementation of this function is provided separately for each platform.
     *
     * @param newConfiguration The desired network configuration change.
     * @return A [NetworkConfigurationChangeResult] indicating success or failure.
     */
    suspend actual fun modifyNetworkConfiguration(newConfiguration: NetworkConfigurationChange): NetworkConfigurationChangeResult{
        TODO("")
    }
}