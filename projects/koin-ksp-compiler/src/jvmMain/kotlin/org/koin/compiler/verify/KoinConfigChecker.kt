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
import org.koin.compiler.resolver.tagAlreadyExists
import org.koin.compiler.resolver.tagPropAlreadyExists
import org.koin.compiler.scanner.ext.getArgument
import org.koin.compiler.scanner.ext.getScopeArgument
import org.koin.compiler.scanner.ext.getValueArgument
import org.koin.compiler.type.clearPackageSymbols
import org.koin.compiler.type.fullWhiteList

const val codeGenerationPackage = "org.koin.ksp.generated"

data class DefinitionVerification(val value: String, val dependencies: ArrayList<String>?, val scope: String? ,val binds: ArrayList<String>?, val qualifier: String?)

/**
 * Koin Configuration Checker
 */
class KoinConfigChecker(val logger: KSPLogger, val resolver: Resolver) {


    fun verifyMetaModules(metaModules: List<KSAnnotation>) {
        metaModules
            .mapNotNull(::extractMetaModuleValues)
            .forEach { (value,includes) ->
                if (!includes.isNullOrEmpty()) verifyMetaModule(value,includes)
            }
    }

    private fun verifyMetaModule(value: String, includes: ArrayList<String>) {
        includes.forEach { i ->
            val exists = resolver.tagAlreadyExists(i.camelCase())
            if (!exists) {
                logger.error("--> Missing Module Definition :'${i}' included in '$value'. Fix your configuration: add @Module annotation on the class.")
            }
        }
    }

    private fun extractMetaModuleValues(a: KSAnnotation): Pair<String, ArrayList<String>?>? {
        val value = a.arguments.getValueArgument()
        val includes = if (value != null) a.arguments.getArray("includes") else null
        return value?.let { it to includes }
    }

    fun verifyMetaDefinitions(metaDefinitions: List<KSAnnotation>) {
        // First extract all the definitions from annotations.
        val definitions = metaDefinitions.mapNotNull(::extractMetaDefinitionValues)
        val allScopes = definitions.mapNotNull { it.scope }
        val allBinds = definitions.filter { !it.binds.isNullOrEmpty() }.flatMap { if (it.qualifier == null) it.binds!! else it.binds!!.mapNotNull { b -> "${b}$KOIN_TAG_SEPARATOR$QUALIFIER_SYMBOL${it.qualifier}" }}
        val availableTypes = (allScopes + allBinds + definitions.map { it.value }).toSet().toList()

        // Verify that each dependency is defined.
        definitions.forEach {
            if (!it.dependencies.isNullOrEmpty()) verifyMetaDefinition(it, availableTypes)
        }
        // Now run cycle detection on the dependency graph.
        //detectDependencyCycles(definitions)
    }

    private fun verifyMetaDefinition(dv: DefinitionVerification, availableTypes: List<String>) {
        dv.dependencies?.forEach { i ->
            val tagData = i.split(":")
            val name = tagData[0]
            val type = tagData[1].clearPackageSymbols()
            val tag = type.camelCase()
            val exists = if (dv.scope == null){ tagAlreadyExists(tag) } else { tagAlreadyExists(tag) || tagAlreadyExists(TagFactory.updateTagWithScope(tag,dv)) }
            if (!exists && type !in fullWhiteList && (type !in availableTypes)) {
                logger.error("--> Missing Definition for property '$name : $type' in '${dv.value}'. Fix your configuration: add definition annotation on the class.")
            }
        }
    }

    private fun tagAlreadyExists(tag : String) : Boolean{
        return resolver.tagAlreadyExists(tag) || resolver.tagPropAlreadyExists(tag)
    }

    private fun extractMetaDefinitionValues(a: KSAnnotation): DefinitionVerification? {
        val value = a.arguments.getValueArgument()
        val includes = if (value != null) a.arguments.getArray("dependencies") else null
        val scope = if (value != null) a.arguments.getScopeArgument() else null
        val binds = if (value != null) a.arguments.getArray("binds") else null
        val qualifier = if (value != null) a.arguments.getArgument("qualifier") else null
        return value?.let { DefinitionVerification(value,includes,scope,binds,qualifier) }
    }

    private fun List<KSValueArgument>.getArray(name : String): ArrayList<String>? {
        return firstOrNull { a -> a.name?.asString() == name }?.value as? ArrayList<String>?
    }

    //TODO To be rewritten
//    /**
//     * Build a dependency graph from all definitions and then
//     * perform a DFS to check for cycles.
//     *
//     * Nodes are identified by the normalized (camelCased) value of the definition,
//     * and each edge comes from a dependency string like "name:Type" (where Type is normalized).
//     */
//    internal fun detectDependencyCycles(definitions: List<DefinitionVerification>) {
//        // Graph: Map<NormalizedDefinition, List<NormalizedDependencies>>
//        val graph = mutableMapOf<String, MutableList<String>>()
//        // Mapping from normalized key back to the original definition value.
//        val normalizedToReal = mutableMapOf<String, String>()
//
//        // First, add every definition as a node in the graph.
//        definitions.forEach { def ->
//            val normalizedKey = def.value.camelCase()
//            graph[normalizedKey] = mutableListOf()
//            normalizedToReal[normalizedKey] = def.value
//        }
//
//        // Then, for each definition, add its dependencies as edges.
//        definitions.forEach { def ->
//            val normalizedKey = def.value.camelCase()
//            // Each dependency is given as a string "property:RealType"
//            val deps = def.dependencies?.mapNotNull { dep ->
//                val parts = dep.split(":")
//                if (parts.size < 2) null
//                else {
//                    // Extract the "real" type name from the dependency.
//                    val realDependency = parts[1].clearPackageSymbols()
//                    val normalizedDep = realDependency.camelCase()
//                    // Only add an edge if the dependency is among the definitions.
//                    if (normalizedDep in graph) normalizedDep else null
//                }
//            }?.toMutableList() ?: mutableListOf()
//            graph[normalizedKey] = deps
//        }
//
//        val visited = mutableSetOf<String>()
//        val recStack = mutableSetOf<String>()
//
//        fun dfs(node: String, path: List<String>) {
//            visited.add(node)
//            recStack.add(node)
//            val newPath = path + node
//            for (neighbor in graph[node] ?: emptyList()) {
//                if (neighbor !in visited) {
//                    dfs(neighbor, newPath)
//                } else if (neighbor in recStack) {
//                    // Cycle detected: extract the cycle portion of the path.
//                    val cycleNormalized = (newPath.dropWhile { it != neighbor } + neighbor)
//                    // Map normalized names back to their real type names.
//                    val cycleReal = cycleNormalized.map { normalizedToReal[it] ?: it }
//                    val cycleString = cycleReal.joinToString(" -> ")
//                    logger.error("--> Dependency cycle detected: $cycleString")
//                }
//            }
//            recStack.remove(node)
//        }
//
//        // Start DFS from every node.
//        for (node in graph.keys) {
//            if (node !in visited) {
//                dfs(node, emptyList())
//            }
//        }
//    }
}

internal fun KSDeclaration.qualifiedNameCamelCase() = qualifiedName?.asString()?.split(".")?.joinToString(separator = "") { it.capitalize() }
