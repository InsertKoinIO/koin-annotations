package org.koin.compiler.metadata.tag

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.metadata.KoinMetaData

class TagResolver {
    lateinit var resolver: Resolver

    fun tagExists(app : KoinMetaData.Application) : Boolean {
        return resolveKSDeclaration(TagFactory.generateTag(app)) != null
    }

    fun tagExists(module : KoinMetaData.Module) : Boolean {
        return resolveKSDeclaration(TagFactory.generateTag(module)) != null
    }

    fun tagExists(def : KoinMetaData.Definition) : Boolean {
        return resolveKSDeclaration(TagFactory.generateTag(def)) != null
    }

    fun tagPropertyExists(tag : String) : Boolean {
        return resolveKSPropertyDeclaration(tag) != null
    }

    fun resolveKSDeclaration(tag : String) : KSDeclaration?{
        val name = TagFactory.prefixTag(tag, withGenPackage = true)
        val declaration = resolver.getClassDeclarationByName(resolver.getKSNameFromString(name))
//        LOGGER.warn("[DEBUG] resolveKSDeclaration '$name' ? ${declaration != null}")
        return declaration
    }

//    // Compat with KSP1
//    //TODO change for property once KSP2
//    // getPropertyDeclarationByName(getKSNameFromString(name),true)
//    fun resolveKSFunctionDeclaration(name : String) : Sequence<KSFunctionDeclaration> {
//        return resolver.getFunctionDeclarationsByName(resolver.getKSNameFromString(name),true)
//    }

    fun resolveKSPropertyDeclaration(tag : String) : KSPropertyDeclaration? {
        val name = TagFactory.prefixTag(tag, withGenPackage = true)
        val declaration = resolver.getPropertyDeclarationByName(resolver.getKSNameFromString(name), true)
//        LOGGER.warn("[DEBUG] resolveKSPropertyDeclaration '$name' ? ${declaration != null}")
        return declaration
    }
}