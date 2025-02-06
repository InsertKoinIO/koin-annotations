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
import org.koin.compiler.metadata.TagFactory
import org.koin.compiler.metadata.camelCase
import org.koin.compiler.resolver.isAlreadyExisting
import org.koin.compiler.scanner.ext.getScopeArgument
import org.koin.compiler.scanner.ext.getValueArgument
import org.koin.compiler.type.clearPackageSymbols
import org.koin.compiler.type.fullWhiteList

const val codeGenerationPackage = "org.koin.ksp.generated"

data class DefinitionVerification(val value: String, val dependencies: ArrayList<String>?, val scope: String?)

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
            val exists = resolver.isAlreadyExisting(i.camelCase())
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
            .forEach {
                if (!it.dependencies.isNullOrEmpty()) verifyMetaDefinition(it)
            }
    }

    private fun verifyMetaDefinition(dv : DefinitionVerification) {
        dv.dependencies?.forEach { i ->
            val tagData = i.split(":")
            val name = tagData[0]
            val type = tagData[1].clearPackageSymbols()
            val tag = type.camelCase()
            val exists = if (dv.scope == null) resolver.isAlreadyExisting(tag) else resolver.isAlreadyExisting(tag) || resolver.isAlreadyExisting(TagFactory.updateTagWithScope(tag,dv))
            if (!exists && type !in fullWhiteList) {
                logger.error("--> Missing Definition for property '$name : $type' in '${dv.value}'. Fix your configuration: add definition annotation on the class.")
            }
        }
    }

    private fun extractMetaDefinitionValues(a: KSAnnotation): DefinitionVerification? {
        val value = a.arguments.getValueArgument()
        val includes = if (value != null) a.arguments.getArray("dependencies") else null
        val scope = if (value != null) a.arguments.getScopeArgument() else null
        return value?.let { DefinitionVerification(value,includes,scope) }
    }

    private fun List<KSValueArgument>.getArray(name : String): ArrayList<String>? {
        return firstOrNull { a -> a.name?.asString() == name }?.value as? ArrayList<String>?
    }
}

internal fun KSDeclaration.qualifiedNameCamelCase() = qualifiedName?.asString()?.split(".")?.joinToString(separator = "") { it.capitalize() }
