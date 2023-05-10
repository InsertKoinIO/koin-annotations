/*
 * Copyright 2017-2023 the original author or authors.
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
package org.koin.compiler.scanner

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import org.koin.compiler.metadata.DEFINITION_ANNOTATION_LIST_TYPES
import org.koin.compiler.metadata.KoinMetaData
import org.koin.core.annotation.Module

class KoinMetaDataScanner(
    private val logger: KSPLogger
) {

    private val moduleMetadataScanner = ModuleScanner(logger)
    private val componentMetadataScanner = ClassComponentScanner(logger)
    private val functionMetadataScanner = FunctionComponentScanner(logger)

    private var validModuleSymbols = mutableListOf<KSAnnotated>()
    private var validDefinitionSymbols = mutableListOf<KSAnnotated>()

    fun scanSymbols(resolver: Resolver): List<KSAnnotated> {
        val moduleSymbols = resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!).toList()
        val definitionSymbols = DEFINITION_ANNOTATION_LIST_TYPES.flatMap { annotation ->
            resolver.getSymbolsWithAnnotation(annotation.qualifiedName!!)
        }

        validModuleSymbols.addAll(moduleSymbols.filter { it.validate() })
        validDefinitionSymbols.addAll(definitionSymbols.filter { it.validate() })

        val invalidModuleSymbols = moduleSymbols.filter { !it.validate() }
        val invalidDefinitionSymbols = definitionSymbols.filter { !it.validate() }
        val invalidSymbols = invalidModuleSymbols + invalidDefinitionSymbols
        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid definition symbols found.")
            logInvalidEntities(invalidSymbols)
            return invalidSymbols
        }

        logger.logging("All symbols are valid")
        return emptyList()
    }

    fun extractKoinMetaData(defaultModule: KoinMetaData.Module): List<KoinMetaData.Module> {
        val moduleList = scanClassModules()
        val index = moduleList.generateScanComponentIndex()
        scanClassComponents(defaultModule, index)
        scanFunctionComponents(defaultModule, index)
        return moduleList
    }

    private fun scanClassModules(): List<KoinMetaData.Module> {
        logger.logging("scan modules ...")
        return validModuleSymbols
            .filterIsInstance<KSClassDeclaration>()
            .map { moduleMetadataScanner.createClassModule(it) }
            .toList()
    }

    private fun List<KoinMetaData.Module>.generateScanComponentIndex(): List<KoinMetaData.Module> {
        val moduleList = hashMapOf<String, KoinMetaData.Module>()
        val emptyScanList = arrayListOf<KoinMetaData.Module>()
        forEach { module ->
            module.componentScan?.let { scan ->
                when (scan.packageName) {
                    "" -> emptyScanList.add(module)
                    else -> if (moduleList.contains(scan.packageName)) {
                        val existing = moduleList[scan.packageName]!!
                        error("@ComponentScan with '${scan.packageName}' from module ${module.name} is already declared in ${existing.name}. Please fix @ComponentScan value ")
                    } else {
                        moduleList[scan.packageName] = module
                    }
                }
            }
        }
        return moduleList.values + emptyScanList
    }

    private fun scanFunctionComponents(
        defaultModule: KoinMetaData.Module,
        scanComponentIndex: List<KoinMetaData.Module>
    ): List<KoinMetaData.Definition> {
        logger.logging("scan functions ...")

        val definitions = validDefinitionSymbols
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { functionMetadataScanner.createFunctionDefinition(it) }
            .toList()

        definitions.forEach { addToModule(it, defaultModule, scanComponentIndex) }
        return definitions
    }

    private fun scanClassComponents(
        defaultModule: KoinMetaData.Module,
        scanComponentIndex: List<KoinMetaData.Module>
    ): List<KoinMetaData.Definition> {
        logger.logging("scan definitions ...")

        val definitions = validDefinitionSymbols
            .filterIsInstance<KSClassDeclaration>()
            .map { componentMetadataScanner.createClassDefinition(it) }
            .toList()
        definitions.forEach { addToModule(it, defaultModule, scanComponentIndex) }
        return definitions
    }

    private fun addToModule(
        definition: KoinMetaData.Definition,
        defaultModule: KoinMetaData.Module,
        modules: List<KoinMetaData.Module>
    ) {
        val definitionPackage = definition.packageName
        val foundModule = modules.firstOrNull { it.acceptDefinition(definitionPackage) } ?: defaultModule
        val alreadyExists = foundModule.definitions.contains(definition)
        if (!alreadyExists) {
            if (foundModule == defaultModule) {
                logger.warn("adding ${definition.label} to default module")
            }
            foundModule.definitions.add(definition)
        } else {
            logger.logging("skip addToModule - definition(class) -> $definition -> module $foundModule - already exists")
        }
    }

    private fun logInvalidEntities(classDeclarationList: List<KSAnnotated>) {
        classDeclarationList.forEach { logger.logging("Invalid entity: $it") }
    }
}
