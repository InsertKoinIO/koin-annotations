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
package org.koin.compiler.generator

import org.koin.compiler.generator.ext.appendText
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.KoinMetaData.Module.Companion.DEFINE_PREFIX
import org.koin.compiler.metadata.SINGLE
import org.koin.compiler.scanner.ext.filterForbiddenKeywords
import org.koin.compiler.verify.ext.getResolution
import java.io.OutputStream

class DefinitionWriter(
    val resolver: Resolver,
    val fileStream: OutputStream
) {
    private fun write(s: String) { fileStream.appendText(s) }
    private fun writeln(s: String) = write("$s\n")

    fun writeDefinition(def: KoinMetaData.Definition, isExternalDefinition: Boolean = false, prefix: String) {

        if (def.alreadyGenerated == null){
            def.alreadyGenerated = canResolveType(def)
        }

        if (def.alreadyGenerated == true){
            LOGGER.logging("skip ${def.label} -> ${def.getTagName()} - already generated")
        } else {
            if (def.isExpect.not()){
                LOGGER.logging("write definition ${def.label} ...")

                val param = def.parameters.generateParamFunction()
                val ctor = generateConstructor(def.parameters)
                val binds = generateBindings(def.bindings)
                val qualifier = def.qualifier.generateQualifier()
                val createAtStart = if (def.isType(SINGLE) && def.isCreatedAtStart == true) {
                    if (qualifier == "") CREATED_AT_START else ",$CREATED_AT_START"
                } else ""
                val space = if (def.isScoped()) TAB + TAB else TAB

                if (isExternalDefinition) {
                    writeExternalDefinitionFunction(def, qualifier, createAtStart, param, prefix, ctor, binds)
                }
                else {
                    writeDefinition(space, def, qualifier, createAtStart, param, prefix, ctor, binds)
                }
            } else {
                LOGGER.logging("skip ${def.label} - isExpect")
            }
        }
    }

    private fun canResolveType(def: KoinMetaData.Definition): Boolean = resolver.getResolution(def) != null

    private fun writeDefinition(
        space: String,
        def: KoinMetaData.Definition,
        qualifier: String,
        createAtStart: String,
        param: String,
        prefix: String,
        ctor: String,
        binds: String
    ) {
        writeln("$space${def.keyword.keyword}($qualifier$createAtStart) { ${param}${prefix}$ctor } $binds")
    }

    private fun writeExternalDefinitionFunction(
        def: KoinMetaData.Definition,
        qualifier: String,
        createAtStart: String,
        param: String,
        prefix: String,
        ctor: String,
        binds: String
    ) {
        writeln("@Definition(\"${def.packageName}\")")
        writeln("public fun Module.$DEFINE_PREFIX${def.label}() : KoinDefinition<*> = ${def.keyword.keyword}($qualifier$createAtStart) { ${param}${prefix}$ctor } $binds")
    }

    private fun List<KoinMetaData.DefinitionParameter>.generateParamFunction(): String {
        return if (any { it is KoinMetaData.DefinitionParameter.ParameterInject }) "params -> " else "_ -> "
    }

    private fun String?.generateQualifier(): String = when {
        this == "\"null\"" -> ""
        this == "null" -> ""
        !this.isNullOrBlank() -> "qualifier=org.koin.core.qualifier.StringQualifier(\"$this\")"
        else -> ""
    }

    val BLOCKED_TYPES = listOf("Any", "ViewModel", "CoroutineWorker", "ListenableWorker")

    private fun generateBindings(bindings: List<KSDeclaration>): String {
        val validBindings = bindings.filter {
            val clazzName = it.simpleName.asString()
            clazzName !in BLOCKED_TYPES
        }

        return when {
            validBindings.isEmpty() -> ""
            validBindings.size == 1 -> {
                val generateBinding = generateBinding(validBindings.first())
                "bind($generateBinding)"
            }

            else -> validBindings.joinToString(prefix = "binds(arrayOf(", separator = ",", postfix = "))") {
                generateBinding(it)
            }
        }
    }

    private fun generateBinding(declaration: KSDeclaration): String {
        val packageName = declaration.packageName.asString().filterForbiddenKeywords()
        val className = declaration.simpleName.asString()
        val parents = getParentDeclarations(declaration)
        return if (parents.isNotEmpty()) {
            val parentNames = parents.joinToString(".") { it.simpleName.asString() }
            "$packageName.$parentNames.$className::class"
        } else {
            "$packageName.$className::class"
        }
    }

    private fun getParentDeclarations(declaration: KSDeclaration): List<KSDeclaration> {
        val parents = mutableListOf<KSDeclaration>()

        var parent = declaration.parentDeclaration
        while (parent != null) {
            parents.add(parent)
            parent = parent.parentDeclaration
        }

        return parents.reversed()
    }

    private fun generateConstructor(constructorParameters: List<KoinMetaData.DefinitionParameter>): String {
        val paramsWithoutDefaultValues = constructorParameters.filter { !it.hasDefault || it is KoinMetaData.DefinitionParameter.Property}
        return paramsWithoutDefaultValues.joinToString(prefix = "(", separator = ",", postfix = ")") { ctorParam ->
            val isNullable: Boolean = ctorParam.nullable
            when (ctorParam) {
                is KoinMetaData.DefinitionParameter.Dependency -> {
                    val scopeId = ctorParam.scopeId
                    when (ctorParam.kind) {
                        KoinMetaData.DependencyKind.List -> "getAll()"
                        else -> {
                            val keyword = when (ctorParam.kind) {
                                KoinMetaData.DependencyKind.Lazy -> "inject"
                                else -> "get"
                            }
                            val qualifier =
                                ctorParam.qualifier?.let { "qualifier=org.koin.core.qualifier.StringQualifier(\"${it}\")" }
                                    ?: ""
                            val operator = if (!isNullable) "$keyword($qualifier)" else "${keyword}OrNull($qualifier)"
                            val scopeOperator = scopeId?.let { "getScope(\"$scopeId\").$operator" } ?: operator
                            if (ctorParam.name == null) scopeOperator else "${ctorParam.name}=$scopeOperator"
                        }
                    }
                }

                is KoinMetaData.DefinitionParameter.ParameterInject -> if (!isNullable) "params.get()" else "params.getOrNull()"
                is KoinMetaData.DefinitionParameter.Property -> {
                    val defaultValue = ctorParam.defaultValue?.let { ",${it.field}" } ?: ""
                    if (!isNullable) "getProperty(\"${ctorParam.value}\"$defaultValue)" else "getPropertyOrNull(\"${ctorParam.value}\",$defaultValue)"
                }
            }
        }
    }

    companion object {
        const val TAB = "\t"
        const val CREATED_AT_START = "createdAtStart=true"
    }
}