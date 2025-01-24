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
import org.koin.compiler.resolver.getResolutionForTag
import org.koin.compiler.resolver.isAlreadyExisting
import org.koin.compiler.scanner.ext.getValueArgument

const val codeGenerationPackage = "org.koin.ksp.generated"

/**
 * Koin Configuration Checker
 */
class KoinConfigChecker(val logger: KSPLogger, val resolver: Resolver) {

//    fun verify(allModules: List<KoinMetaData.Module>) {
//        verifyDefinitionDeclarations(allModules)
//        verifyModuleIncludes(allModules)
//    }

//    private fun verifyDefinitionDeclarations(moduleList: List<KoinMetaData.Module>) {
//        val allDefinitions = moduleList.flatMap { it.definitions }
//        verifyDependencies(allDefinitions)
//    }

//    private fun verifyDependencies(
//        allDefinitions: List<KoinMetaData.Definition>
//    ) {
//        allDefinitions.forEach { def ->
//            def.parameters
//                .filterIsInstance<KoinMetaData.DefinitionParameter.Dependency>()
//                .forEach { param ->
//                    checkDependency(param, def)
//                    //TODO Check Cycle
//                }
//        }
//    }

//    private fun checkDependency(
//        param: KoinMetaData.DefinitionParameter.Dependency,
//        def: KoinMetaData.Definition
//    ) {
//        if (!param.hasDefault && !param.isNullable && !param.alreadyProvided) {
//            checkDependencyIsDefined(param, def)
//        }
//    }


//    private fun checkDependencyIsDefined(
//        dependencyToCheck: KoinMetaData.DefinitionParameter.Dependency,
//        definition: KoinMetaData.Definition,
//    ) {
//        val label = definition.label
//        val scope = (definition.scope as? KoinMetaData.Scope.ClassScope)?.type?.qualifiedName?.asString()
//        var targetTypeToCheck: KSDeclaration = dependencyToCheck.type.declaration
//
//        if (targetTypeToCheck.simpleName.asString() == "List" || targetTypeToCheck.simpleName.asString() == "Lazy") {
//            targetTypeToCheck =
//                dependencyToCheck.type.arguments.firstOrNull()?.type?.resolve()?.declaration ?: targetTypeToCheck
//        }
//
//        val parameterFullName = targetTypeToCheck.qualifiedName?.asString()
//        if (parameterFullName !in typeWhiteList && parameterFullName != null) {
//            val cn = targetTypeToCheck.qualifiedNameCamelCase()
//            val resolution = resolver.getResolutionForTag(cn)
//            val isNotScopeType = scope != parameterFullName
//            if (resolution == null && isNotScopeType) {
//                logger.error("--> Missing Definition type '$parameterFullName' for '${definition.packageName}.$label'. Fix your configuration to define type '${targetTypeToCheck.simpleName.asString()}'.")
//            }
//        }
//    }

//    fun verifyModuleIncludes(modules: List<KoinMetaData.Module>) {
//        modules.forEach { m ->
//            val mn = m.packageName + "." + m.name
//            m.includes?.forEach { inc ->
//                val prop = resolver.getResolutionForTag(TagFactory.getTag(inc))
//                if (prop == null) {
//                    logger.error("--> Module Undefined :'${inc.className}' included in '$mn'. Fix your configuration: add @Module annotation on '${inc.className}' class.")
//                }
//            }
//        }
//    }

    fun verifyMetaModules(metaModules: List<KSAnnotation>) {
        metaModules
            .mapNotNull(::extractMetaModuleValues)
            .forEach { (value,includes) ->
                if (!includes.isNullOrEmpty()) verifyMetaModule(value,includes)
            }
    }

    private fun verifyMetaModule(value: String, includes: ArrayList<String>) {
//        logger.warn("verifyMetaModule: m:$value - i:$includes")
        includes.forEach { i ->
            val exists = resolver.isAlreadyExisting(i)
//            logger.warn("verifyMetaModule: m:$value -> $i = $exists")
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
        metaDefinitions
            .mapNotNull(::extractMetaDefinitionValues)
            .forEach { (value,dependencies) ->
//                logger.warn("verifyMetaDefinitions: def:$value - deps:$dependencies")
                if (!dependencies.isNullOrEmpty()) verifyMetaDefinition(value,dependencies)
            }
    }

    private fun verifyMetaDefinition(value: String, dependencies: ArrayList<String>) {
//        logger.warn("verifyMetaDefinition: def:$value - i:$dependencies")
        dependencies.forEach { i ->
            val exists = resolver.isAlreadyExisting(i)
            if (!exists) {
                logger.warn("verifyMetaDefinition: def:$value -> $i = $exists")
                logger.warn("--> Missing Definition :'${i}' used by '$value'. Fix your configuration: add @Module annotation on the class.")
            }
        }
    }

    private fun extractMetaDefinitionValues(a: KSAnnotation): Pair<String, ArrayList<String>?>? {
        val value = a.arguments.getValueArgument()
        val includes = if (value != null) a.arguments.getArray("dependencies") else null
        return value?.let { it to includes }
    }

    private fun List<KSValueArgument>.getArray(name : String): ArrayList<String>? {
        return firstOrNull { a -> a.name?.asString() == name }?.value as? ArrayList<String>?
    }
}

internal fun KSDeclaration.qualifiedNameCamelCase() = qualifiedName?.asString()?.split(".")?.joinToString(separator = "") { it.capitalize() }
