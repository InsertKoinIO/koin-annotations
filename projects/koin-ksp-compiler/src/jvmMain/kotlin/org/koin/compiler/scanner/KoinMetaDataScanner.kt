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
package org.koin.compiler.scanner

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.metadata.DEFINITION_ANNOTATION_LIST_TYPES
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.util.anyMatch
import org.koin.core.annotation.Module
import org.koin.core.annotation.PropertyValue
import org.koin.meta.annotations.ExternalDefinition

class KoinMetaDataScanner(
    private val logger: KSPLogger
) {

    private val moduleMetadataScanner = ModuleScanner(logger)
    private val componentMetadataScanner = ClassComponentScanner(logger)
    private val functionMetadataScanner = FunctionComponentScanner(logger)

    fun findInvalidSymbols(resolver: Resolver): List<KSAnnotated> {
        val invalidModuleSymbols = resolver.getInvalidModuleSymbols()
        val invalidDefinitionSymbols = resolver.getInvalidDefinitionSymbols()

        val invalidSymbols = invalidModuleSymbols + invalidDefinitionSymbols
        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid definition symbols found.")
            logInvalidEntities(invalidSymbols)
            return invalidSymbols
        }

        return emptyList()
    }

    fun scanKoinModules(
        defaultModule: KoinMetaData.Module,
        resolver: Resolver
    ): List<KoinMetaData.Module> {
        val moduleList = scanClassModules(resolver)
        val index = moduleList.generateScanComponentIndex()
        scanClassComponents(defaultModule, index, resolver)
        scanFunctionComponents(defaultModule, index, resolver)
        scanDefaultProperties(index+defaultModule, resolver)
        scanExternalDefinitions(index, resolver)
        return moduleList
    }

    private fun scanDefaultProperties(
        index: List<KoinMetaData.Module>,
        resolver: Resolver
    ) {
        val propertyValues: List<KoinMetaData.PropertyValue> = resolver.getValidPropertySymbols()
            .mapNotNull { def ->
            def.annotations
                .first { it.shortName.asString() == PropertyValue::class.simpleName }
                .let { a ->
                    val id = a.arguments.first().value?.toString()
                    val field = (a.parent as? KSDeclaration)
                    id?.let { field?.qualifiedName?.asString()?.let { KoinMetaData.PropertyValue(id = id ,  it) } }
                }
        }

        val allProperties = index
            .flatMap { it.definitions }
            .flatMap { it.parameters }
            .filterIsInstance<KoinMetaData.DefinitionParameter.Property>()

        //associate default values
        propertyValues
            .forEach { propertyValue ->
                allProperties.filter { it.value == propertyValue.id }.forEach {
                    it.defaultValue = propertyValue
                }
            }
    }

    private fun scanExternalDefinitions(
        index: List<KoinMetaData.Module>,
        resolver: Resolver
    ) {
        resolver.getExternalDefinitionSymbols()
            .filter { !it.isExpect }
            .mapNotNull { definitionDeclaration ->
                definitionDeclaration.annotations
                    .first { it.shortName.asString() == DEFINITION_ANNOTATION }.arguments.first().value?.toString()
                    ?.let { packageValue ->
                        KoinMetaData.ExternalDefinition(targetPackage = packageValue, name = definitionDeclaration.simpleName.asString())
                    }
            }
            .forEach { extDef ->
                val module = index.firstOrNull { it.acceptDefinition(extDef.targetPackage) }
                module?.externalDefinitions?.add(extDef)
            }
    }

    private fun scanClassModules(resolver: Resolver): List<KoinMetaData.Module> {
        logger.logging("scan modules ...")
        return resolver.getValidModuleSymbols()
            .filterIsInstance<KSClassDeclaration>()
            .map { moduleMetadataScanner.createClassModule(it) }
            .toList()
    }

    private fun List<KoinMetaData.Module>.generateScanComponentIndex(): List<KoinMetaData.Module> {
        val moduleList = hashMapOf<String, KoinMetaData.Module>()
        val emptyScanList = arrayListOf<KoinMetaData.Module>()
        forEach { module ->
            module.componentsScan.forEach { scan ->
                when (scan.packageName) {
                    "" -> emptyScanList.add(module)
                    else -> if (moduleList.anyMatch(scan.packageName)) {
                        if (module.isActual || !module.isExpect){
                            moduleList[scan.packageName] = module
                        }
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
        scanComponentIndex: List<KoinMetaData.Module>,
        resolver: Resolver
    ): List<KoinMetaData.Definition> {
        logger.logging("scan functions ...")

        val definitions = resolver.getValidDefinitionSymbols()
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { functionMetadataScanner.createFunctionDefinition(it) }
            .toList()

        definitions.forEach { addToModule(it, defaultModule, scanComponentIndex) }
        return definitions
    }

    private fun scanClassComponents(
        defaultModule: KoinMetaData.Module,
        scanComponentIndex: List<KoinMetaData.Module>,
        resolver: Resolver
    ): List<KoinMetaData.Definition> {
        logger.logging("scan definitions ...")

        val definitions = resolver.getValidDefinitionSymbols()
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
                logger.info("No module found for '$definitionPackage.${definition.label}'. Definition is added to 'defaultModule'")
            }
            foundModule.definitions.add(definition)
        } else {
            logger.logging("skip addToModule - definition(class) -> $definition -> module $foundModule - already exists")
        }
    }

    private fun logInvalidEntities(classDeclarationList: List<KSAnnotated>) {
        classDeclarationList.forEach { logger.logging("Invalid entity: $it") }
    }

    private fun Resolver.getInvalidModuleSymbols(): List<KSAnnotated> {
        return this.getSymbolsWithAnnotation(Module::class.qualifiedName!!)
            .filter { !it.validate() }
            .toList()
    }

    private fun Resolver.getInvalidDefinitionSymbols(): List<KSAnnotated> {
        return DEFINITION_ANNOTATION_LIST_TYPES.flatMap { annotation ->
            this.getSymbolsWithAnnotation(annotation.qualifiedName!!)
                .filter { !it.validate() }
        }
    }

    private fun Resolver.getValidModuleSymbols(): List<KSAnnotated> {
        return this.getSymbolsWithAnnotation(Module::class.qualifiedName!!)
            .filter { it.validate() }
            .toList()
    }

    private fun Resolver.getValidDefinitionSymbols(): List<KSAnnotated> {
        return DEFINITION_ANNOTATION_LIST_TYPES.flatMap { annotation ->
            this.getSymbolsWithAnnotation(annotation.qualifiedName!!)
                .filter { it.validate() }
        }
    }

    private fun Resolver.getValidPropertySymbols(): List<KSAnnotated> {
        return this.getSymbolsWithAnnotation(PropertyValue::class.qualifiedName!!)
            .filter { it.validate() }
            .toList()
    }

    @OptIn(KspExperimental::class)
    private fun Resolver.getExternalDefinitionSymbols(): List<KSDeclaration> {
        return this.getDeclarationsFromPackage("org.koin.ksp.generated")
            .filter { a -> a.annotations.any { it.shortName.asString() == DEFINITION_ANNOTATION } }
            .toList()
    }

    companion object {
        private val DEFINITION_ANNOTATION = ExternalDefinition::class.simpleName
    }
}
