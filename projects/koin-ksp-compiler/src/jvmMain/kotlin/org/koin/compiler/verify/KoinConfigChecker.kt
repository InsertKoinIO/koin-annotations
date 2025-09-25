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
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.metadata.tag.TagFactory
import org.koin.compiler.metadata.camelCase
import org.koin.compiler.metadata.tag.TagResolver
import org.koin.compiler.scanner.KoinMetaDataScanner
import org.koin.compiler.scanner.ext.getArgument
import org.koin.compiler.scanner.ext.getScopeArgument
import org.koin.compiler.scanner.ext.getValueArgument
import org.koin.compiler.type.clearPackageSymbols
import org.koin.compiler.type.fullWhiteList
import kotlin.error

data class MetaDefinitionAnnotationData(val value: String, val moduleTagId : String, val dependencies: ArrayList<String>?, val scope: String?, val binds: ArrayList<String>?, val qualifier: String?)
data class MetaDefinitionData(val value: String, val module : MetaModuleData?, val dependencies: List<String>?, val scope: String?, val binds: List<String>?, val qualifier: String?)

data class MetaModuleAnnotationData(val value: String, val tag : String, val id : String, val includes: ArrayList<String>?, val configurations: ArrayList<String>?)
data class MetaModuleData(val value: String, val tag : String, val id : String, val includes: ArrayList<MetaModuleData>, val configurations: List<String>){
    var parentModule : MetaModuleData? = null
}

data class MetaApplicationAnnotationData(val value: String, val includes: ArrayList<String>?, val configurations: ArrayList<String>?)
data class MetaApplicationData(val value: String, val includes: List<MetaModuleData>, val configurations: List<MetaModuleData>)

private const val DEFAULT_MODULE_NAME = "DefaultModule"

/**
 * Koin Configuration Checker
 */
class KoinConfigChecker(val logger: KSPLogger, val tagResolver: TagResolver) {

    private lateinit var modulesById : MutableMap<String, MetaModuleData>
    private lateinit var modulesByValue: MutableMap<String, MetaModuleData>
    private lateinit var modulesByTag: MutableMap<String, MetaModuleData>
    private lateinit var allLocalDefinitions : MutableMap<String, MetaDefinitionData>
    private lateinit var definitionsByValue: MutableMap<String, MetaDefinitionData>

    fun verify(foundMetaModules: List<KSAnnotation>, foundMetaDefinitions: List<KSAnnotation>) { //, foundMetaApplications: List<KSAnnotation>) {
        val metaModuleByValue = foundMetaModules.mapNotNull(::extractMetaModuleValues).associateBy { it.value }
        val modules = metaModuleByValue.values.map { module ->
            mapToModule(module)
        }
        
        // Build all module indexes for O(1) lookup
        modulesById = modules.associateBy { it.id }.toMutableMap()
        modulesByValue = modules.associateBy { it.value }.toMutableMap()
        modulesByTag = modules.associateBy { it.tag }.toMutableMap()

        val modulesToProcess = modulesById.values.toList()
        modulesToProcess.forEach { module ->
            // resolve includes
            val metaModule = metaModuleByValue[module.value]!!
            resolveModuleIncludes(module, metaModule)
        }

        val definitions = foundMetaDefinitions.mapNotNull(::extractMetaDefinitionValues).map { metaDefinition ->
            mapToDefinition(metaDefinition)
        }

        // Build definition indexes for O(1) lookup
        definitionsByValue = definitions.associateBy { it.value }.toMutableMap()
        
        val definitionTypes = definitions.associateBy { it.value }
        val scopes = definitions.filter { it.scope != null }.associateBy { it.scope!! }
        val binds = definitions.filter { !it.binds.isNullOrEmpty() }.flatMap { def ->
            val t = if (def.qualifier == null) def.binds!! else def.binds!!.map { b -> TagFactory.createTagForQualifier(b,def) }
            t.map { it to def }
        }.toMap()
        allLocalDefinitions = ((definitionTypes + scopes + binds) as MutableMap<String, MetaDefinitionData>)

        definitions.forEach { def -> verifyDefinition(def)}
        detectDependencyCycles(definitions)
    }

    private fun findExternalModule(
        revertedTag: String,
        symbol: String
    ): Pair<MetaModuleData, MetaModuleAnnotationData>? {
        if (revertedTag == DEFAULT_MODULE_NAME) {
            return null
        }

        // find external module by tag
        val declaration = resolveTagDeclarationForModule(revertedTag)
        // found external module
        return declaration?.let { declaration ->
            // extract meta
            val metaModule = extractMetaModuleValues(declaration.annotations.first())
                ?: error("can't find module metadata for '$symbol' on ${declaration.qualifiedName?.asString()}")
            val newModule = mapToModule(metaModule)
            modulesById[newModule.id] = newModule
            newModule to metaModule
            // add to modulesById
        } ?: error("can't find module metadata for '$symbol' in current modules or any meta tags")
    }

    private fun resolveModuleIncludes(module: MetaModuleData, metaModule: MetaModuleAnnotationData){
        // get includes
        val includes = metaModule.includes

        // map includes
        module.includes.addAll( includes?.mapNotNull { includeSymbol ->
            findModuleForSymbol(includeSymbol, module)
        } ?: emptyList())
    }

    private fun findModuleForSymbol(
        includeSymbol: String,
        module: MetaModuleData? = null,
    ): MetaModuleData? {
        // O(1) lookup by value
        var found = modulesByValue[includeSymbol]
        if (found == null) {
            val moduleTag = reverTag(includeSymbol)
            // O(1) lookup by tag
            found = modulesByTag[moduleTag]
            if (found == null) {
                val foundExternalModule = findExternalModule(moduleTag, includeSymbol)
                if (foundExternalModule != null) {
                    found = foundExternalModule.first
                    resolveModuleIncludes(found, foundExternalModule.second)
                    // Update indexes with external module
                    modulesByValue[found.value] = found
                    modulesByTag[found.tag] = found
                }
            }
        }
        found?.parentModule = module
        return found
    }

    private fun mapToDefinition(
        metaDefinition: MetaDefinitionAnnotationData,
    ): MetaDefinitionData {
        val (moduleId, moduleTag) = metaDefinition.moduleTagId.split(":").let { it[0] to it[1] }
        return MetaDefinitionData(
            metaDefinition.value,
            modulesById[moduleId] ?: findExternalModule(moduleTag, metaDefinition.value)
                ?.let {
                    resolveModuleIncludes(it.first, it.second)
                    it.first
                     },
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
        arrayListOf(),
        module.configurations ?: emptyList()
    )

    fun reverTag(t : String) = t.split(".").joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }

    private fun extractMetaModuleValues(a: KSAnnotation): MetaModuleAnnotationData? {
        val parentTag = (a.parent as? KSDeclaration)?.simpleName?.asString() ?: ""
        val value = a.arguments.getValueArgument()
        val id = a.arguments.getArgument("id") ?: ""
        val includes = if (value != null) a.arguments.getArray("includes") else null
        val configs = if (value != null) a.arguments.getArray("configurations") else null
        return value?.let { MetaModuleAnnotationData(value, parentTag, id,includes,configs) }
    }

    private fun extractMetaApplicationValues(a: KSAnnotation): MetaApplicationAnnotationData? {
        val value = a.arguments.getValueArgument()
        val includes = if (value != null) a.arguments.getArray("includes") else null
        val configs = if (value != null) a.arguments.getArray("configurations") else null
        return value?.let { MetaApplicationAnnotationData(value,includes,configs) }
    }

    private fun mapToApplicationData(a: MetaApplicationAnnotationData): MetaApplicationData {
        return MetaApplicationData(
            a.value,
            a.includes?.mapNotNull { findModuleForSymbol(it) } ?: emptyList(),
            a.configurations?.mapNotNull { findModuleForSymbol(it) } ?: emptyList()
        )
    }

    private fun verifyDefinition(
        def: MetaDefinitionData
    ) {
        if (def.dependencies?.isNotEmpty() == true){
            def.dependencies.forEach { dep -> verifyDefinition( def, dep) }
        }
    }

    private fun verifyDefinition(
        def: MetaDefinitionData,
        dependency: String
    ) {
        val tagData = dependency.split(":")
        val parameterName = tagData[0]
        val parameterType = tagData[1].clearPackageSymbols()
        val tag = parameterType.camelCase()

        if (tag.endsWith("_Actual")){
            return
        }

        val foundInLocalDefinitions = (parameterType in fullWhiteList || (parameterType in allLocalDefinitions.keys))
        if (foundInLocalDefinitions) {
            allLocalDefinitions[parameterType]?.let { found ->
                isDependencyDefined(def,found, dependency)
            }
        }

        if (!foundInLocalDefinitions) {
            // resolve definition by tag
            val foundResolution = resolveTagDeclarationForDefinition(tag) ?: if (def.scope != null) { resolveTagDeclarationForDefinition(TagFactory.createTagForScope(tag, def)) } else null

            // found definition resolution
            if (foundResolution == null){
                val paramType = parameterType.removeSuffix("_Expect")
                logger.error("--> Missing definition for '$parameterName : $paramType' in '${def.value}'. Fix by adding a definition annotation or function definition in module class.")
            }
            else {
                val definition = extractMetaDefinitionValues(foundResolution.annotations.firstOrNull() ?: error("can't find definition metadata for $tag on ${foundResolution.qualifiedName?.asString()}") )
                    ?.let { mapToDefinition(it) }
                    ?: error("can't extract definition metadata for $tag on ${foundResolution.qualifiedName?.asString()}")

                addDefinitionToCurrentSymbols(definition)
                isDependencyDefined(def, definition, dependency)
            }
        }
    }

    private fun addDefinitionToCurrentSymbols(definition: MetaDefinitionData) {
        val defByValue = definition.value to definition
        val defByBindings = definition.binds?.map { it to definition } ?: emptyList()
        val defByScope = definition.scope?.let { it to definition }
        val allSymbols = listOfNotNull(defByBindings + defByValue + defByScope).flatten().filterNotNull().toMap()
        allLocalDefinitions.putAll(allSymbols)
    }

    private fun isDependencyDefined(
        initialDefinition: MetaDefinitionData,
        dependencyDefinition: MetaDefinitionData,
        property: String
    ) {
        val initialModule = initialDefinition.module
        val targetModule = dependencyDefinition.module
        if (targetModule != null){
            val isAccessible = isModuleAccessible(initialModule,targetModule) || isModuleAccessibleFromParent(initialModule,targetModule) || isModuleAccessibleInConfiguration(initialModule,targetModule)
            if (!isAccessible){
                logger.error("--> Unreachable definition '$property' in '${initialDefinition.value}'. Fix your modules and configuration.")
            }
        }
    }

    private fun isModuleAccessibleInConfiguration(
        initialModule: MetaModuleData?,
        targetModule: MetaModuleData
    ) : Boolean {
        if (initialModule == null) return false
        return targetModule.configurations.any { it in initialModule.configurations } || isModuleAccessibleInConfiguration(initialModule.parentModule, targetModule)
    }

    private fun isModuleAccessibleFromParent(
        initialModule: MetaModuleData?,
        targetModule: MetaModuleData
    ) : Boolean {
        return if (initialModule?.parentModule == null) false
        else {
            isModuleAccessible(initialModule.parentModule,targetModule) || isModuleAccessibleFromParent(initialModule.parentModule,targetModule)
        }
    }

    //TODO to change with configurations
    private fun isModuleAccessible(
        initialModule: MetaModuleData?,
        targetModule: MetaModuleData
    ) : Boolean {
        return if (initialModule?.value == targetModule.value) true
        else if (initialModule == null) false
        else {
            if (initialModule.includes.isEmpty()) false
            else targetModule in initialModule.includes || initialModule.includes.any { isModuleAccessible(it,targetModule) }
        }
    }

    private fun resolveTagDeclarationForModule(tag : String) : KSDeclaration?{
        return tagResolver.resolveKSDeclaration(tag)
    }

    private fun resolveTagDeclarationForDefinition(tag : String) : KSDeclaration?{
        return tagResolver.resolveKSDeclaration(tag) ?: tagResolver.resolveKSPropertyDeclaration(tag)
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
    ): MetaDefinitionData? {
        // First try O(1) lookup by value
        definitionsByValue[tag]?.let { return it }
        // Fallback to O(n) lookup for binds
        return definitions.firstOrNull { it.binds?.contains(tag) == true }
    }

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

        // O(1) lookup by value instead of O(n) linear search
        val currentDefinition = definitionsByValue[currentTag] ?: return

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
