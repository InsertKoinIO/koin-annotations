package com.jetbrains.kmpapp.nativetest

import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import kotlin.coroutines.CoroutineContext

@Module
@ComponentScan
class NativeTestModule

interface ConfigurationModifier {
    val coroutineContext: CoroutineContext

    fun getAllNetworkProfiles(): Flow<List<NetworkProfile>>

    suspend fun addNetworkProfile(networkProfile: NetworkProfile): Boolean

    suspend fun applyProfileToDevice(
        networkProfile: NetworkProfile,
        device: Device
    ): ProfileApplicationResult
}

class NetworkProfile
class Device
class ProfileApplicationResult

@Single(binds = [ConfigurationModifier::class])
internal class ConfigurationModifierImpl(
    @InjectedParam
    override val coroutineContext: CoroutineContext,
    private val networkConfigurationModifier: NetworkConfigurationModifier
) : ConfigurationModifier {
    override fun getAllNetworkProfiles(): Flow<List<NetworkProfile>> {
        TODO("Not yet implemented")
    }

    override suspend fun addNetworkProfile(networkProfile: NetworkProfile): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun applyProfileToDevice(
        networkProfile: NetworkProfile,
        device: Device
    ): ProfileApplicationResult {
        TODO("Not yet implemented")
    }
}

class NetworkConfigurationChange
class NetworkConfigurationChangeResult

@Single
expect class NetworkConfigurationModifier(){

    /**
     * Modifies the network configuration based on the platform-specific implementation.
     *
     * The actual implementation of this function is provided separately for each platform.
     *
     * @param newConfiguration The desired network configuration change.
     * @return A [NetworkConfigurationChangeResult] indicating success or failure.
     */
    suspend fun modifyNetworkConfiguration(newConfiguration: NetworkConfigurationChange): NetworkConfigurationChangeResult
}