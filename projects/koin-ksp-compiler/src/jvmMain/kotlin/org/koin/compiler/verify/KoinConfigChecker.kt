/*
 * Copyright 2017-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.koin.compiler.verify

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import org.koin.compiler.metadata.KOIN_TAG_SEPARATOR
import org.koin.compiler.metadata.QUALIFIER_SYMBOL
import org.koin.compiler.metadata.TAG_PREFIX
import org.koin.compiler.metadata.TagFactory
import org.koin.compiler.metadata.camelCase
import org.koin.compiler.resolver.getResolutionForTag
import org.koin.compiler.resolver.getResolutionForTagProp
import org.koin.compiler.resolver.tagAlreadyExists
import org.koin.compiler.resolver.tagPropAlreadyExists
import org.koin.compiler.scanner.ext.getArgument
import org.koin.compiler.scanner.ext.getScopeArgument
import org.koin.compiler.scanner.ext.getValueArgument
import org.koin.compiler.type.clearPackageSymbols
import org.koin.compiler.type.fullWhiteList
import kotlin.error

const val codeGenerationPackage = "org.koin.ksp.generated"

data class MetaDefinitionAnnotationData(val value: String, val moduleTagId : String, val dependencies: ArrayList<String>?, val scope: String?, val binds: ArrayList<String>?, val qualifier: String?)
data class MetaDefinitionData(val value: String, val module : MetaModuleData, val dependencies: List<String>?, val scope: String?, val binds: List<String>?, val qualifier: String?)

data class MetaModuleAnnotationData(val value: String, val tag : String, val id : String, val includes: ArrayList<String>?, val configurations: ArrayList<String>?)
data class MetaModuleData(val value: String, val tag : String, val id : String, var includes: List<MetaModuleData>?, val configurations: List<String>?)

/**
 * Koin Configuration Checker
 */
class KoinConfigChecker(val logger: KSPLogger, val resolver: Resolver) {

    private lateinit var modulesById : MutableMap<String, MetaModuleData>
    private lateinit var allLocalDefinitions : MutableMap<String, MetaDefinitionData>

    fun extractData(foundMetaModules: List<KSAnnotation>, foundMetaDefinitions: List<KSAnnotation>) {
        val metaModuleByValue = foundMetaModules.mapNotNull(::extractMetaModuleValues).associateBy { it.value }
        modulesById = metaModuleByValue.values.map { module ->
            mapToModule(module)
        }.associateBy { it.id }.toMutableMap()

        val modulesToProcess = modulesById.values.toList()
        modulesToProcess.forEach { module ->
            // fill includes
            if (module.includes != null){
                val metaModule = metaModuleByValue[module.value]!!
                val includes = metaModule.includes
                // map includes
                module.includes = includes?.map { inc ->
                    //found in current modules?
                    var found = modulesById.values.firstOrNull { it.value == inc }
                    if (found == null){
                        val revertedTag = reverTag(inc)
                        // find by current modules tag
                        found = modulesById.values.firstOrNull { it.tag == revertedTag }
                        if (found == null){
                            found = findExternalModule(revertedTag, inc)
                        }
                    }
                    found
                }
            }
        }

        logger.warn("[DEBUG] modules:\n${modulesById.values.joinToString(",\n") { module -> "${module.value} -> ${module.includes?.map { include -> include.value }}" }}")

        val definitions = foundMetaDefinitions.mapNotNull(::extractMetaDefinitionValues).map { metaDefinition ->
            mapToDefinition(metaDefinition)
        }
        val definitionTypes = definitions.associateBy { it.value }
        val scopes = definitions.filter { it.scope != null }.associateBy { it.scope!! }
        val binds = definitions.filter { !it.binds.isNullOrEmpty() }.flatMap { def ->
            val t = if (def.qualifier == null) def.binds!! else def.binds!!.map { b -> "${b}$KOIN_TAG_SEPARATOR$QUALIFIER_SYMBOL${def.qualifier}" }
            t.map { it to def }
        }.toMap()
        allLocalDefinitions = ((definitionTypes + scopes + binds) as MutableMap<String, MetaDefinitionData>)

        logger.warn("[DEBUG] definitions:\n${allLocalDefinitions.map { (k,v) -> "$k -> ${v.value}" }.joinToString(",\n")}")

        definitions.forEach { def -> verifyDefinition(def)}
        detectDependencyCycles(definitions)
    }

    private fun findExternalModule(
        revertedTag: String,
        symbol: String
    ): MetaModuleData {
        // find external module by tag
        val declaration = resolveTagDeclarationForModule(revertedTag)
        // found external module
        return declaration?.let { declaration ->
            // extract meta
            val metaModule = extractMetaModuleValues(declaration.annotations.first())
                ?: error("can't find module metadata for $symbol on ${declaration.qualifiedName?.asString()}")
            val newModule = mapToModule(metaModule)
            modulesById[newModule.id] = newModule
            newModule
            // add to modulesById
        } ?: error("can't find module metadata for $symbol in current modules or any meta tags")
    }

    private fun mapToDefinition(
        metaDefinition: MetaDefinitionAnnotationData,
    ): MetaDefinitionData {
        val (moduleId, moduleTag) = metaDefinition.moduleTagId.split(":").let { it[0] to it[1] }
        return MetaDefinitionData(
            metaDefinition.value,
            modulesById[moduleId] ?: findExternalModule(TAG_PREFIX+moduleTag, metaDefinition.value),
            metaDefinition.dependencies,
            metaDefinition.scope,
            metaDefinition.binds,
            metaDefinition.qualifier
        )
    }

    private fun mapToModule(module: MetaModuleAnnotationData): MetaModuleData = MetaModuleData(
        module.value,
        module.tag,
        module.id,
        if (module.includes == null) null else emptyList(),
        module.configurations
    )

    fun reverTag(t : String) = TAG_PREFIX+t.split(".").joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }

    private fun extractMetaModuleValues(a: KSAnnotation): MetaModuleAnnotationData? {
        val parentTag = (a.parent as? KSDeclaration)?.simpleName?.asString() ?: ""
        val value = a.arguments.getValueArgument()
        val id = a.arguments.getArgument("id") ?: ""
        val includes = if (value != null) a.arguments.getArray("includes") else null
        val configs = if (value != null) a.arguments.getArray("configurations") else null
        return value?.let { MetaModuleAnnotationData(value, parentTag, id,includes,configs) }
    }

    private fun verifyDefinition(
        def: MetaDefinitionData
    ) {
        if (def.dependencies?.isNotEmpty() == true){
            logger.warn("[DEBUG] verify - ${def.value}")
            def.dependencies.forEach { dep -> verifyDefinition( def, dep) }
        }
    }

    private fun verifyDefinition(
        def: MetaDefinitionData,
        dependency: String
    ) {
        logger.warn("[DEBUG] dependency '$dependency'")

        val tagData = dependency.split(":")
        val parameterName = tagData[0]
        val parameterType = tagData[1].clearPackageSymbols()
        val tag = parameterType.camelCase()

        val foundInLocalDefinitions = (parameterType in fullWhiteList || (parameterType in allLocalDefinitions.keys))
        if (foundInLocalDefinitions) {
            allLocalDefinitions[parameterType]?.let { found ->
                logger.warn("[DEBUG] found dependency definition - $found")
            }
        }

        if (!foundInLocalDefinitions) {
            val foundResolution = if (def.scope == null) {
                resolveTagDeclarationForDefinition(tag)
            } else {
                resolveTagDeclarationForDefinition(tag) ?: resolveTagDeclarationForDefinition(TagFactory.updateTagWithScope(tag, def))
            }
            logger.warn("[DEBUG] found resolution? ${foundResolution != null}")
            if (foundResolution == null){
                logger.error("--> Missing Definition for property '$parameterName : $parameterType' in '${def.value}'. Fix your configuration: add definition annotation on the class.")
            }
            else {
                val definition = extractMetaDefinitionValues(foundResolution.annotations.firstOrNull() ?: error("can't find definition metadata for $tag on ${foundResolution.qualifiedName?.asString()}") )
                    ?.let { mapToDefinition(it) }
                    ?: error("can't extract definition metadata for $tag on ${foundResolution.qualifiedName?.asString()}")
                //TODO definition is in right module scope (path)

    //            val foudMetaDef = foundResolution?.annotations?.firstOrNull { it.shortName.asString() == MetaDefinition::class.simpleName!! }


    //            if (foudMetaDef != null) {
    //                val foundModuleId = foudMetaDef.arguments.getArgument("moduleId")
    //                println("[DEBUG] foundModuleId - ${foundResolution.qualifiedName?.asString()} - $foundModuleId}")
    //
    //                if (foundModuleId != null) {
    //                    val foundGroup = moduleGroups.firstOrNull { it.contains(foundModuleId) } ?: error("module foundModuleId '$foundModuleId' not found in any group")
    //                    println("[DEBUG] foundInTypes - ${foundResolution.qualifiedName?.asString()} - ${foudMetaDef.arguments} - Module id? ${foundGroup == defGroup}")
    //                } else {
    //                    println("[DEBUG] foundInTypes - ${foundResolution.qualifiedName?.asString()} - ${foudMetaDef.arguments} - no Module id")
    //                }
    //            }
    //            if (foundResolution == null) {
    //                logger.error("--> Missing Definition for property '$parameterName : $parameterType' in '${def.value}'. Fix your configuration: add definition annotation on the class.")
    //            }
            }
        }
    }

    private fun resolveTagDeclarationForModule(tag : String) : KSDeclaration?{
        return resolver.getResolutionForTag(tag, addTagPrefix = false)
    }

    private fun resolveTagDeclarationForDefinition(tag : String) : KSDeclaration?{
        return resolver.getResolutionForTag(tag) ?: resolver.getResolutionForTagProp(tag).firstOrNull()
    }

    private fun tagAlreadyExists(tag : String) : Boolean{
        return resolver.tagAlreadyExists(tag) || resolver.tagPropAlreadyExists(tag)
    }

    private fun extractMetaDefinitionValues(a: KSAnnotation): MetaDefinitionAnnotationData? {
        val value = a.arguments.getValueArgument()
        val moduleTagId = a.arguments.getArgument("moduleTagId") ?: error("can't find moduleTagId in MetaDefinitionData in $a")
        val includes = if (value != null) a.arguments.getArray("dependencies") else null
        val scope = if (value != null) a.arguments.getScopeArgument() else null
        val binds = if (value != null) a.arguments.getArray("binds") else null
        val qualifier = if (value != null) a.arguments.getArgument("qualifier") else null
        return value?.let { MetaDefinitionAnnotationData(value,moduleTagId,includes,scope,binds,qualifier) }
    }

    private fun List<KSValueArgument>.getArray(name : String): ArrayList<String>? {
        return firstOrNull { a -> a.name?.asString() == name }?.value as? ArrayList<String>?
    }

    /**
     * Build a dependency graph from all definitions and then
     * perform a DFS to check for cycles.
     *
     * Nodes are identified by the normalized (camelCased) value of the definition,
     * and each edge comes from a dependency string like "name:Type" (where Type is normalized).
     */
    internal fun detectDependencyCycles(definitions: List<MetaDefinitionData>) {
        try {
            definitions.forEach { definition ->
                if (definition.dependencies?.isNotEmpty() == true) {
                    definition.dependencies.forEach { dep ->
                        try {
                            val tag = cleanUpTag(dep)
                            val found = findDefinitionFromTag(definitions, tag)
                            found?.let {
                                if (!found.dependencies.isNullOrEmpty()) {
                                    val visited = mutableSetOf<String>()
                                    visited.add(definition.value)
                                    lookupCycle(definitions, definition, tag, visited)
                                }
                            }
                        } catch (e: Exception) {
                            logger.error("Error processing dependency '$dep' in '${definition.value}': ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanUpTag(dep: String): String {
        return try {
            val parts = dep.split(":")
            if (parts.size < 2) {
                throw IllegalArgumentException("Invalid dependency format: $dep")
            }
            parts[1].split("_")[0]
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse dependency: $dep", e)
        }
    }

    private fun findDefinitionFromTag(
        definitions: List<MetaDefinitionData>,
        tag: String
    ): MetaDefinitionData? = definitions.firstOrNull { it.binds?.contains(tag) == true || it.value.contains(tag) }

    private fun lookupCycle(
        allDefinitions: List<MetaDefinitionData>,
        originDefinition: MetaDefinitionData,
        currentTag: String,
        visited: MutableSet<String>
    ) {
        // Prevent infinite recursion
        if (visited.size > allDefinitions.size) {
            logger.error("---> Maximum recursion depth reached while checking dependencies for '${originDefinition.value}'")
            return
        }

        val currentDefinition = allDefinitions.firstOrNull { it.value == currentTag } ?: return

        // If we've seen this node before, we have a cycle
        if (currentDefinition.value in visited) {
            // Report any cycle, not just back to origin
            val cycleStart = visited.toList().indexOf(currentDefinition.value)
            val cyclePath = visited.toList().subList(cycleStart, visited.size).joinToString(" -> ") + " -> " + currentDefinition.value
            logger.error("---> Cycle detected: $cyclePath")
            return
        }

        visited.add(currentDefinition.value)

        currentDefinition.dependencies?.forEach { dep ->
            try {
                val nextTag = cleanUpTag(dep)
                lookupCycle(allDefinitions, originDefinition, nextTag, visited)
            } catch (e: Exception) {
                logger.error("Error processing dependency '$dep' in '${currentDefinition.value}': ${e.message}")
            }
        }

        visited.remove(currentDefinition.value)
    }
}

internal fun KSDeclaration.qualifiedNameCamelCase() = qualifiedName?.asString()?.split(".")?.joinToString(separator = "") { part -> part.replaceFirstChar { char -> char.uppercase() } }
