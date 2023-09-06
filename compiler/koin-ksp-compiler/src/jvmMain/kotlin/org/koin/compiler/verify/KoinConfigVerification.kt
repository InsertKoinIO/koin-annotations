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

import appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.generator.getFile
import org.koin.compiler.metadata.KoinMetaData
import java.io.OutputStream

private val ignored = listOf("kotlin.Any",)
private val classPrefix = "KoinDef"
private val generationPackage = "org.koin.ksp.generated"

/**
 * Koin Configuration Checker
 */
class KoinConfigVerification(val codeGenerator: CodeGenerator, val logger: KSPLogger) {

    fun verifyDefinitionDeclarations(
        moduleList: List<KoinMetaData.Module>,
        resolver: Resolver
    ) {
        val noGenFile = codeGenerator.generatedFile.isEmpty()
        val alreadyDeclared = arrayListOf<String>()

        moduleList
            .flatMap { it.definitions }
            .forEach { def ->
                if (noGenFile) {
                    def.parameters
                        .filterIsInstance<KoinMetaData.ConstructorParameter.Dependency>()
                        .forEach { param ->
                            if (!param.hasDefault){
                                checkDependencyIsDefined(param, resolver, def)
                            }

                            //TODO Check Cycle
                        }
                } else {
                    writeDefinitionTag(def, alreadyDeclared)
                }
            }
    }

    private fun writeDefinitionTag(
        def: KoinMetaData.Definition,
        alreadyDeclared: ArrayList<String>
    ) {
        val label = def.label
        val className = label.capitalize()
        val fileName = def.packageCamelCase() + className

        val fileStream = codeGenerator.getFile(fileName = fileName)
        fileStream.appendText("package $generationPackage")

        // tag for Class & Functions
        writeClassTag(def, alreadyDeclared, fileStream)

        def.bindings.forEach { writeDefinitionBindingTag(it, alreadyDeclared, fileStream) }
    }

    private fun writeClassTag(
        def: KoinMetaData.Definition,
        alreadyDeclared: java.util.ArrayList<String>,
        fileStream: OutputStream
    ) {
        val cn = def.packageCamelCase() + def.label.capitalize()

        if (cn !in alreadyDeclared) {
            val write = "public class $classPrefix$cn"

            fileStream.appendText("\n$write")
            alreadyDeclared.add(cn)
        }
    }

    private fun writeDefinitionBindingTag(
        binding: KSDeclaration,
        alreadyDeclared: ArrayList<String>,
        fileStream: OutputStream
    ) {
        if (binding.qualifiedName?.asString() !in ignored) {
            val cn = binding.qualifiedNameCamelCase()

            if (cn !in alreadyDeclared && cn != null) {
                val write = "public class $classPrefix$cn"
                fileStream.appendText("\n$write")
                alreadyDeclared.add(cn)
            }
        }
    }

    private fun checkDependencyIsDefined(
        dependencyToCheck: KoinMetaData.ConstructorParameter.Dependency,
        resolver: Resolver,
        definition: KoinMetaData.Definition,
    ) {
        val label = definition.label
        val scope = (definition.scope as? KoinMetaData.Scope.ClassScope)?.type?.qualifiedName?.asString()
        var targetTypeToCheck: KSDeclaration = dependencyToCheck.type.declaration

        if (targetTypeToCheck.simpleName.asString() == "List" || targetTypeToCheck.simpleName.asString() == "Lazy"){
            targetTypeToCheck = dependencyToCheck.type.arguments.firstOrNull()?.type?.resolve()?.declaration ?: targetTypeToCheck
        }

        val parameterFullName = targetTypeToCheck.qualifiedName?.asString()
        if (parameterFullName !in ignored && parameterFullName != null) {
            val cn = targetTypeToCheck.qualifiedNameCamelCase()
            val className = "$generationPackage.$classPrefix$cn"
            val resolution = resolver.getClassDeclarationByName(resolver.getKSNameFromString(className))
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
                    val cn = inc.qualifiedName?.asString()?.replace(".", "_")
                    val ksn = resolver.getKSNameFromString("$generationPackage.$cn")
                    val prop = resolver.getPropertyDeclarationByName(ksn, includeTopLevel = true)
                    if (prop == null) {
                        logger.error("--> Module Undefined :'${inc.qualifiedName?.asString()}' included in '$mn'. Fix your configuration: add @Module annotation on '${inc.simpleName.asString()}' class.")
                    }
                }
            }
        }
    }
}

private fun KoinMetaData.Definition.packageCamelCase() = packageName.split(".").joinToString("") { it.capitalize() }
private fun KSDeclaration.qualifiedNameCamelCase() =
    qualifiedName?.asString()?.split(".")?.joinToString(separator = "") { it.capitalize() }
