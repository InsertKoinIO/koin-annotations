package org.koin.compiler.generator

import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.tag.TagFactory

fun KoinCodeGenerator.generateProxies(monitoredDefinitions: List<KoinMetaData.Definition.ClassDefinition>) {
    monitoredDefinitions.forEach(::generateProxy)
}

fun KoinCodeGenerator.generateProxy(monitoredDefinition: KoinMetaData.Definition.ClassDefinition) {
    //TODO check if already generated
    val resolver = tagResolver.resolver
    val proxyClass = TagFactory.DEFAULT_GEN_PACKAGE + "." + monitoredDefinition.className + "Proxy"
    val generatedProxy = resolver.getClassDeclarationByName(resolver.getKSNameFromString(proxyClass))
    if (generatedProxy == null){
        MonitoredProxyClassWriter(codeGenerator,tagResolver.resolver, monitoredDefinition).writeProxy()
    }
}