package org.koin.compiler.metadata.tag

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.metadata.KoinMetaData

class TagResolver {
    lateinit var resolver: Resolver
    private val tagExistenceCache = mutableMapOf<String, Boolean>()

    fun tagExists(app : KoinMetaData.Application) : Boolean {
        val tag = TagFactory.generateTag(app)
        return tagExistenceCache.getOrPut(tag) {
            resolveKSDeclaration(tag) != null
        }
    }

    fun tagExists(module : KoinMetaData.Module) : Boolean {
        val tag = TagFactory.generateTag(module)
        return tagExistenceCache.getOrPut(tag) {
            resolveKSDeclaration(tag) != null
        }
    }

    fun tagExists(def : KoinMetaData.Definition) : Boolean {
        val tag = TagFactory.generateTag(def)
        return tagExistenceCache.getOrPut(tag) {
            resolveKSDeclaration(tag) != null
        }
    }

    fun tagPropertyExists(tag : String) : Boolean {
        return tagExistenceCache.getOrPut("prop:$tag") {
            resolveKSPropertyDeclaration(tag) != null
        }
    }

    fun batchCheckTagsExist(modules: List<KoinMetaData.Module>, definitions: List<KoinMetaData.Definition>, applications: List<KoinMetaData.Application>) {
        val allTags = mutableSetOf<String>()
        
        modules.forEach { module ->
            allTags.add(TagFactory.generateTag(module))
        }
        
        definitions.forEach { def ->
            allTags.add(TagFactory.generateTag(def))
        }
        
        applications.forEach { app ->
            allTags.add(TagFactory.generateTag(app))
        }
        
        // Batch check all tags that aren't cached
        val uncachedTags = allTags.filter { it !in tagExistenceCache }
        uncachedTags.forEach { tag ->
            tagExistenceCache[tag] = resolveKSDeclaration(tag) != null
        }
    }

    fun resolveKSDeclaration(tag : String) : KSDeclaration?{
        val name = TagFactory.prefixTag(tag, withGenPackage = true)
        val declaration = resolver.getClassDeclarationByName(resolver.getKSNameFromString(name))
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
        return declaration
    }
}