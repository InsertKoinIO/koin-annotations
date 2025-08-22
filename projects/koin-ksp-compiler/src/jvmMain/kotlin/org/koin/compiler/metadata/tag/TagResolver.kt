package org.koin.compiler.metadata.tag

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.verify.codeGenerationPackage

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

    fun resolveKSDeclaration(tag : String?, addTagPrefix : Boolean = true) : KSDeclaration?{
        return resolveKSDeclaration(if (addTagPrefix) "$codeGenerationPackage.$TAG_PREFIX$tag" else "$codeGenerationPackage.$tag")
    }

    fun tagPropertyExists(tag : String) : Boolean {
        return resolveKSPropertyDeclaration("$codeGenerationPackage.$TAG_PREFIX$tag") != null
    }

    fun resolveKSDeclaration(name : String) : KSDeclaration?{
        return resolver.getClassDeclarationByName(resolver.getKSNameFromString(name))
    }

//    // Compat with KSP1
//    //TODO change for property once KSP2
//    // getPropertyDeclarationByName(getKSNameFromString(name),true)
//    fun resolveKSFunctionDeclaration(name : String) : Sequence<KSFunctionDeclaration> {
//        return resolver.getFunctionDeclarationsByName(resolver.getKSNameFromString(name),true)
//    }

    fun resolveKSPropertyDeclaration(name : String) : KSPropertyDeclaration? {
        return resolver.getPropertyDeclarationByName(resolver.getKSNameFromString(name), true)
    }
}