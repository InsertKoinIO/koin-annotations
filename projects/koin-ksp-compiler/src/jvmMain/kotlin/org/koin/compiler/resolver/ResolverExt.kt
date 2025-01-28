package org.koin.compiler.resolver

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.TagFactory
import org.koin.compiler.metadata.TAG_PREFIX
import org.koin.compiler.verify.codeGenerationPackage

fun Resolver.isAlreadyExisting(mod : KoinMetaData.Module) : Boolean {
    return getResolution(mod) != null
}

fun Resolver.getResolution(mod : KoinMetaData.Module) : KSDeclaration?{
    return getResolutionForTag(TagFactory.getTag(mod))
}

fun Resolver.isAlreadyExisting(def : KoinMetaData.Definition) : Boolean {
    return getResolution(def) != null
}

fun Resolver.getResolution(def : KoinMetaData.Definition) : KSDeclaration?{
    return getResolutionForTag(TagFactory.getTagClass(def))
}

fun Resolver.isAlreadyExisting(tag : String?) : Boolean {
    return getResolutionForTag(tag) != null
}

fun Resolver.getResolutionForTag(tag : String?) : KSDeclaration?{
    return getResolutionForClass("$codeGenerationPackage.$TAG_PREFIX$tag")
}

fun Resolver.getResolutionForClass(name : String) : KSDeclaration?{
    return getClassDeclarationByName(getKSNameFromString(name))
}