package org.koin.compiler.verify.ext

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.verify.codeGenerationPackage
import org.koin.compiler.verify.tagPrefix


fun Resolver.getResolution(mod : KoinMetaData.Module) : KSDeclaration?{
    return getResolutionForTag(mod.getTagName())
}

fun Resolver.getResolution(def : KoinMetaData.Definition) : KSDeclaration?{
    return getResolutionForTag(def.getTagName())
}

fun Resolver.getResolutionForTag(tag : String?) : KSDeclaration?{
    return getResolutionForClass("$codeGenerationPackage.$tagPrefix$tag")
}

fun Resolver.getResolutionForClass(name : String) : KSDeclaration?{
    return getClassDeclarationByName(getKSNameFromString(name))
}