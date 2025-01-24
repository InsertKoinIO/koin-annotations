package org.koin.compiler.verify

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.TagFactory
import org.koin.compiler.metadata.tagPrefix


fun Resolver.getResolution(mod : KoinMetaData.Module) : KSDeclaration?{
    return getResolutionForTag(TagFactory.getTagName(mod))
}

fun Resolver.getResolution(def : KoinMetaData.Definition) : KSDeclaration?{
    return getResolutionForTag(TagFactory.getTagName(def))
}

fun Resolver.getResolutionForTag(tag : String?) : KSDeclaration?{
    return getResolutionForClass("$codeGenerationPackage.$tagPrefix$tag")
}

fun Resolver.getResolutionForClass(name : String) : KSDeclaration?{
    return getClassDeclarationByName(getKSNameFromString(name))
}