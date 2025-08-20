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
import org.koin.meta.annotations.MetaDefinition

const val codeGenerationPackage = "org.koin.ksp.generated"

data class MetaDefinitionData(val value: String, val moduleId : String,val dependencies: ArrayList<String>?, val scope: String?, val binds: ArrayList<String>?, val qualifier: String?)
data class MetaModuleData(val value: String, val id : String, val includes: ArrayList<String>?, val configurations: ArrayList<String>?)

/**
 * Koin Configuration Checker
 */
class KoinConfigChecker(val logger: KSPLogger, val resolver: Resolver) {

    fun extractData(foundMetaModules: List<KSAnnotation>, foundMetaDefinitions: List<KSAnnotation>) {
        val metaModules = foundMetaModules.mapNotNull(::extractMetaModuleValues)
        val moduleGroups: List<Set<String>> = metaModules.map { module ->
            buildSet {
                add(module.id)
                module.includes?.forEach { includeValue ->
                    metaModules.find { it.value == includeValue }?.id?.let { add(it) }
                }
            }
        }
        println("[DEBUG] metaModules - $metaModules ")
        println("[DEBUG] moduleGroups - $moduleGroups ")

        val definitions = foundMetaDefinitions.mapNotNull(::extractMetaDefinitionValues)

        val definitionTypes = definitions.associateBy { it.value }
        val scopes = definitions.filter { it.scope != null }.associateBy { it.scope!! }
        val binds = definitions.filter { !it.binds.isNullOrEmpty() }.flatMap { def ->
            val t = if (def.qualifier == null) def.binds!! else def.binds!!.map { b -> "${b}$KOIN_TAG_SEPARATOR$QUALIFIER_SYMBOL${def.qualifier}" }
            t.map { it to def }
        }.toMap()

        val availableTypes = (definitionTypes + scopes + binds)

        definitions.forEach { def -> verifyAllDefinitions(def, availableTypes,moduleGroups)}
        detectDependencyCycles(definitions)
    }

    private fun extractMetaModuleValues(a: KSAnnotation): MetaModuleData? {
        val value = a.arguments.getValueArgument()
        val id = a.arguments.getArgument("id") ?: ""
        val includes = if (value != null) a.arguments.getArray("includes") else null
        val configs = if (value != null) a.arguments.getArray("configurations") else null
        return value?.let { MetaModuleData(value,id,includes,configs) }
    }

    private fun verifyAllDefinitions(
        def: MetaDefinitionData,
        availableTypes: Map<String, MetaDefinitionData>,
        moduleGroups: List<Set<String>>
    ) {
        def.dependencies?.forEach { dep ->
            verifyDefinition(moduleGroups, def, dep, availableTypes)
        }
    }

    private fun verifyDefinition(
        moduleGroups: List<Set<String>>,
        def: MetaDefinitionData,
        dependency: String,
        availableTypes: Map<String, MetaDefinitionData>
    ) {
        val defGroup = moduleGroups.firstOrNull { it.contains(def.moduleId) } ?: error("module '${def.moduleId}' not found in any group")
        val tagData = dependency.split(":")
        val parameterName = tagData[0]
        val parameterType = tagData[1].clearPackageSymbols()
        val tag = parameterType.camelCase()
        val foundInTypes = (parameterType in fullWhiteList || (parameterType in availableTypes.keys))
        if (foundInTypes) {
            availableTypes[parameterType]?.let { found ->
                val foundGroup = moduleGroups.firstOrNull { it.contains(found.moduleId) } ?: error("module '${found.moduleId}' not found in any group")
                println("[DEBUG] foundInTypes - $found. Module id? ${foundGroup == defGroup}")
            }
        }
        if (!foundInTypes) {

            val foundResolution = if (def.scope == null) {
                getTagResolution(tag)
            } else {
                getTagResolution(tag) ?: getTagResolution(TagFactory.updateTagWithScope(tag, def))
            }
            val foudMetaDef =
                foundResolution?.annotations?.firstOrNull { it.shortName.asString() == MetaDefinition::class.simpleName!! }
            if (foudMetaDef != null) {
                val foundModuleId = foudMetaDef.arguments.getArgument("moduleId")
                println("[DEBUG] foundModuleId - ${foundResolution.qualifiedName?.asString()} - $foundModuleId}")

                if (foundModuleId != null) {
                    val foundGroup = moduleGroups.firstOrNull { it.contains(foundModuleId) } ?: error("module foundModuleId '$foundModuleId' not found in any group")
                    println("[DEBUG] foundInTypes - ${foundResolution.qualifiedName?.asString()} - ${foudMetaDef.arguments} - Module id? ${foundGroup == defGroup}")
                } else {
                    println("[DEBUG] foundInTypes - ${foundResolution.qualifiedName?.asString()} - ${foudMetaDef.arguments} - no Module id")
                }
            }
            if (foundResolution == null) {
                logger.error("--> Missing Definition for property '$parameterName : $parameterType' in '${def.value}'. Fix your configuration: add definition annotation on the class.")
            }
        }
    }

    private fun getTagResolution(tag : String) : KSDeclaration?{
        return resolver.getResolutionForTag(tag) ?: resolver.getResolutionForTagProp(tag).firstOrNull()
    }

    private fun tagAlreadyExists(tag : String) : Boolean{
        return resolver.tagAlreadyExists(tag) || resolver.tagPropAlreadyExists(tag)
    }

    private fun extractMetaDefinitionValues(a: KSAnnotation): MetaDefinitionData? {
        val value = a.arguments.getValueArgument()
        val moduleId = a.arguments.getArgument("moduleId") ?: error("can't find module id in MetaDefinitionData in $a")
        val includes = if (value != null) a.arguments.getArray("dependencies") else null
        val scope = if (value != null) a.arguments.getScopeArgument() else null
        val binds = if (value != null) a.arguments.getArray("binds") else null
        val qualifier = if (value != null) a.arguments.getArgument("qualifier") else null
        return value?.let { MetaDefinitionData(value,moduleId,includes,scope,binds,qualifier) }
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

internal fun KSDeclaration.qualifiedNameCamelCase() = qualifiedName?.asString()?.split(".")?.joinToString(separator = "") { it.replaceFirstChar { it.uppercase() } }
