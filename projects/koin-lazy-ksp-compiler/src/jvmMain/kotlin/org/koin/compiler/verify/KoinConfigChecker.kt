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

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.verify.ext.getResolutionForTag

const val codeGenerationPackage = "org.koin.ksp.generated"

/**
 * Koin Configuration Checker
 */
class KoinConfigChecker(val codeGenerator: CodeGenerator, val logger: KSPLogger) {

    fun verifyDefinitionDeclarations(
        moduleList: List<KoinMetaData.Module>,
        resolver: Resolver
    ) {
        val isAlreadyGenerated = codeGenerator.generatedFile.isEmpty()
        val allDefinitions = moduleList.flatMap { it.definitions }

        if (isAlreadyGenerated) {
            verifyDependencies(allDefinitions, resolver)
        }
    }

    private fun verifyDependencies(
        allDefinitions: List<KoinMetaData.Definition>,
        resolver: Resolver
    ) {
        allDefinitions.forEach { def ->
            def.parameters
                .filterIsInstance<KoinMetaData.DefinitionParameter.Dependency>()
                .forEach { param ->
                    checkDependency(param, resolver, def)
                    //TODO Check Cycle
                }
        }
    }

    private fun checkDependency(
        param: KoinMetaData.DefinitionParameter.Dependency,
        resolver: Resolver,
        def: KoinMetaData.Definition
    ) {
        if (!param.hasDefault && !param.isNullable && !param.alreadyProvided) {
            checkDependencyIsDefined(param, resolver, def)
        }
    }


    private fun checkDependencyIsDefined(
        dependencyToCheck: KoinMetaData.DefinitionParameter.Dependency,
        resolver: Resolver,
        definition: KoinMetaData.Definition,
    ) {
        val label = definition.label
        val scope = (definition.scope as? KoinMetaData.Scope.ClassScope)?.type?.qualifiedName?.asString()
        var targetTypeToCheck: KSDeclaration = dependencyToCheck.type.declaration

        if (targetTypeToCheck.simpleName.asString() == "List" || targetTypeToCheck.simpleName.asString() == "Lazy") {
            targetTypeToCheck =
                dependencyToCheck.type.arguments.firstOrNull()?.type?.resolve()?.declaration ?: targetTypeToCheck
        }

        val parameterFullName = targetTypeToCheck.qualifiedName?.asString()
        if (parameterFullName !in typeWhiteList && parameterFullName != null) {
            val cn = targetTypeToCheck.qualifiedNameCamelCase()
            val resolution = resolver.getResolutionForTag(cn)
            val isNotScopeType = scope != parameterFullName
            if (resolution == null && isNotScopeType) {
                logger.error("--> Missing Definition type '$parameterFullName' for '${definition.packageName}.$label'. Fix your configuration to define type '${targetTypeToCheck.simpleName.asString()}'.")
            }
        }
    }

    fun verifyModuleIncludes(modules: List<KoinMetaData.Module>, resolver: Resolver) {
        val noGenFile = codeGenerator.generatedFile.isEmpty()
        if (noGenFile) {
            modules.forEach { m ->
                val mn = m.packageName + "." + m.name
                m.includes?.forEach { inc ->
                    val prop = resolver.getResolutionForTag(inc.getTagName())
                    if (prop == null) {
                        logger.error("--> Module Undefined :'${inc.className}' included in '$mn'. Fix your configuration: add @Module annotation on '${inc.className}' class.")
                    }
                }
            }
        }
    }
}

internal fun KSDeclaration.qualifiedNameCamelCase() = qualifiedName?.asString()?.split(".")?.joinToString(separator = "") { it.capitalize() }
