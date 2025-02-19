package org.koin.example.binds

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

public interface Network
public class NetworkImpl : Network

public interface Call
public class CallImpl : Call

public class ClientWithBinds(public val call : Call, public val network: Network)

@Module
public class BindTestsModule{

    @Single(binds = [Call::class])
    public fun call(): CallImpl = CallImpl()

    @Single(binds = [Network::class])
    public fun network(): NetworkImpl = NetworkImpl()

    @Single
    public fun client(call: Call, network: Network) : ClientWithBinds = ClientWithBinds(call,network)
}