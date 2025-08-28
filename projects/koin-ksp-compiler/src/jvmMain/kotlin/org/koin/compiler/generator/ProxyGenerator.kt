package org.koin.compiler.generator

import org.koin.compiler.metadata.KoinMetaData

fun KoinCodeGenerator.generateProxies(monitoredDefinitions: List<KoinMetaData.Definition.ClassDefinition>) {
    monitoredDefinitions.forEach(::generateProxy)
}

fun KoinCodeGenerator.generateProxy(monitoredDefinition: KoinMetaData.Definition.ClassDefinition) {
    //TODO check if already generated
    MonitoredProxyClassWriter(codeGenerator,tagResolver.resolver, monitoredDefinition).writeProxy()
}