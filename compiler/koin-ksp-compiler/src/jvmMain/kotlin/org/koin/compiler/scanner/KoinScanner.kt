/*
 * Copyright 2017-2022 the original author or authors.
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
import com.google.devtools.ksp.validate
import org.koin.compiler.metadata.DEFINITION_ANNOTATION_LIST_TYPES
import org.koin.compiler.metadata.KoinMetaData
import org.koin.core.annotation.Module

class KoinMetaDataScanner(
    private val logger: KSPLogger
) {

    private val moduleMetadataScanner = ModuleScanner(logger)
    private val componentMetadataScanner = ComponentScanner(logger)

    private var validModuleSymbols = mutableListOf<KSAnnotated>()
    private var validDefinitionSymbols = mutableListOf<KSAnnotated>()

    fun scanSymbols(resolver: Resolver): List<KSAnnotated> {
        val moduleSymbols =
            resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!).toList()
        validModuleSymbols.addAll(moduleSymbols.filter { it.validate() })
        val invalidSymbols = moduleSymbols.filter { !it.validate() }

        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid Module symbols found")
            logInvalidEntities(invalidSymbols)
            return invalidSymbols
        }

        val classSymbols = DEFINITION_ANNOTATION_LIST_TYPES
            .flatMap { annotationClass ->
                resolver.getSymbolsWithAnnotation(annotationClass.qualifiedName!!)
            }
        validDefinitionSymbols.addAll(classSymbols.filter { it.validate() })
        val invalidDefinitionSymbols = classSymbols.filter { !it.validate() }

        if (invalidDefinitionSymbols.isNotEmpty()) {
            logger.logging("Invalid definition symbols found.")
            logInvalidEntities(invalidDefinitionSymbols)
            return invalidDefinitionSymbols
        }

        logger.logging("All symbols are valid")

        return emptyList()
    }

    fun scanAllMetaData(defaultModule: KoinMetaData.Module): List<KoinMetaData.Module> {
        val moduleList = scanClassModules()
        val scanComponentIndex = moduleList.generateScanComponentIndex()
        scanClassComponents(defaultModule, scanComponentIndex)
        return moduleList
    }

    private fun scanClassModules(): List<KoinMetaData.Module> {
        logger.logging("scan modules ...")
        return validModuleSymbols
            .filter { it is KSClassDeclaration && it.validate() }
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

    private fun scanClassComponents(
        defaultModule: KoinMetaData.Module,
        scanComponentIndex: List<KoinMetaData.Module>
    ): List<KoinMetaData.Definition> {
        logger.logging("scan definitions ...")

        val definitions = validDefinitionSymbols
            .filterIsInstance<KSClassDeclaration>()
            .map { componentMetadataScanner.extractClassDefinition(it) }
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
            foundModule.definitions.add(definition)
        } else {
            logger.logging("skip addToModule - definition(class) -> $definition -> module $foundModule - already exists")
        }
    }

    private fun logInvalidEntities(classDeclarationList: List<KSAnnotated>) {
        classDeclarationList
            .map { it as KSClassDeclaration }
            .forEach { classDeclaration ->
                logger.logging("Invalid entity: $classDeclaration")
                logger.logging("   qualifiedName = ${classDeclaration.qualifiedName?.asString()}")
                logger.logging("   classKind = ${classDeclaration.classKind}")
                classDeclaration.superTypes.forEach {
                    logger.logging("   superType = $it")
                }
            }
    }
}
