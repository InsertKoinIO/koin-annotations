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
import kotlin.reflect.KClass

class KoinMetaDataScanner(
    val logger: KSPLogger
) {

    lateinit var moduleMap: ModuleMap
    private val moduleMetadataScanner = ModuleScanner(logger)
    private val componentMetadataScanner = ComponentScanner(logger)

    fun scanAllMetaData(
        resolver: Resolver,
        defaultModule: KoinMetaData.Module
    ): Pair<ModuleMap, List<KoinMetaData.Definition>> {
        return Pair(
            scanClassModules(resolver, defaultModule).toSortedMap(),
            scanComponents(resolver, defaultModule)
        )
    }

    private fun scanClassModules(
        resolver: Resolver,
        defaultModule: KoinMetaData.Module
    ): ModuleMap {

        logger.logging("scan modules ...")
        // class modules
        moduleMap = resolver.getSymbolsWithAnnotation(Module::class.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .map { moduleMetadataScanner.createClassModule(it) }
            .groupBy ({ (key, _) -> key }) { (_, value) -> value }

        return moduleMap
    }

    private fun scanComponents(
        resolver: Resolver,
        defaultModule: KoinMetaData.Module
    ): List<KoinMetaData.Definition> {
        // component scan
        logger.logging("scan definitions ...")
        logger.logging("moduleMap size: ${moduleMap}")
        val definitions =
            DEFINITION_ANNOTATION_LIST_TYPES.flatMap { a -> resolver.scanDefinition(a) { d -> componentMetadataScanner.extractDefinition(d) } }

        definitions.forEach { addToComponentScanningModuleOrDefault(it, defaultModule) }
        return definitions
    }

    private fun Resolver.scanDefinition(
        annotationClass: KClass<*>,
        mapDefinition: (KSAnnotated) -> KoinMetaData.Definition
    ): List<KoinMetaData.Definition> {
        return getSymbolsWithAnnotation(annotationClass.qualifiedName!!)
            .filter { it is KSClassDeclaration && it.validate() }
            .mapNotNull { mapDefinition(it) }
            .toList()
    }

    private fun addToComponentScanningModuleOrDefault(
        definition: KoinMetaData.Definition,
        defaultModule: KoinMetaData.Module
    ) {
        val definitionPackage = definition.packageName
        val foundModule = moduleMap.firstAcceptedDefinitionPackageModuleOrNull(definitionPackage)
        val module = foundModule ?: defaultModule
        val alreadyExists = module.definitions.contains(definition)
        if (!alreadyExists) {
            module.definitions.add(definition)
        } else {
            logger.logging("skip addToModule - definition(class) -> $definition -> module $module - already exists")
        }
    }
    private fun ModuleMap.firstAcceptedDefinitionPackageModuleOrNull(definitionPackage: String): KoinMetaData.Module? {
        return values.firstOrNull() { it.last().acceptDefinition(definitionPackage) }?.last()
    }
}

typealias ModuleIndex = Pair<String, KoinMetaData.Module>
typealias ModuleMap = Map<String, List<KoinMetaData.Module>>
