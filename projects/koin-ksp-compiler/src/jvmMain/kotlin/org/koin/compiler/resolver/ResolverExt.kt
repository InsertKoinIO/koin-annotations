package org.koin.compiler.resolver

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.TagFactory
import org.koin.compiler.metadata.TAG_PREFIX
import org.koin.compiler.verify.codeGenerationPackage

fun Resolver.tagAlreadyExists(mod : KoinMetaData.Module) : Boolean {
    return getResolution(mod) != null
}

fun Resolver.getResolution(mod : KoinMetaData.Module) : KSDeclaration?{
    return getResolutionForTag(TagFactory.getTagClass(mod))
}

fun Resolver.tagAlreadyExists(def : KoinMetaData.Definition) : Boolean {
    return getResolution(def) != null
}

fun Resolver.getResolution(def : KoinMetaData.Definition) : KSDeclaration?{
    return getResolutionForTag(TagFactory.getTagClass(def))
}

fun Resolver.tagAlreadyExists(tag : String?) : Boolean {
    return getResolutionForTag(tag) != null
}

fun Resolver.tagPropAlreadyExists(tag : String?) : Boolean {
    return getResolutionForTagProp(tag).any()
}

fun Resolver.getResolutionForTag(tag : String?, addTagPrefix : Boolean = true) : KSDeclaration?{
    return getResolutionForClass(if (addTagPrefix) "$codeGenerationPackage.$TAG_PREFIX$tag" else "$codeGenerationPackage.$tag")
}

fun Resolver.getResolutionForTagProp(tag : String?) : Sequence<KSFunctionDeclaration> {
    return getResolutionForProp("$codeGenerationPackage.$TAG_PREFIX$tag")
}

fun Resolver.getResolutionForClass(name : String) : KSDeclaration?{
//    LOGGER.warn("[DEBUG] Resolver.getResolutionForClass '$name'")
    return getClassDeclarationByName(getKSNameFromString(name))
}
// Compat with KSP1
//TODO change for property once KSP2
// getPropertyDeclarationByName(getKSNameFromString(name),true)
fun Resolver.getResolutionForProp(name : String) : Sequence<KSFunctionDeclaration> {
    return getFunctionDeclarationsByName(getKSNameFromString(name),true)
}