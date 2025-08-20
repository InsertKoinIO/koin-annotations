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
import org.koin.compiler.metadata.DEFINITION_ANNOTATION_LIST_TYPES
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.KoinMetaData.ModuleInclude
import org.koin.compiler.scanner.ext.getValueArgument
import org.koin.compiler.util.anyMatch
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.PropertyValue
import org.koin.meta.annotations.ExternalDefinition
import org.koin.meta.annotations.MetaModule
import kotlin.collections.component1
import kotlin.collections.component2

class KoinMetaDataScanner(
    private val logger: KSPLogger
) {

    private val applicationMetadataScanner = ApplicationScanner(logger)
    private val moduleMetadataScanner = ModuleScanner(logger)
    private val componentMetadataScanner = ClassComponentScanner(logger)
    private val functionMetadataScanner = FunctionComponentScanner(logger)

    fun findInvalidSymbols(resolver: Resolver): List<KSAnnotated> {
        val invalidModuleSymbols = resolver.getInvalidSymbols<Module>()
        val invalidDefinitionSymbols = resolver.getInvalidDefinitionSymbols()

        val invalidSymbols = invalidModuleSymbols + invalidDefinitionSymbols
        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid definition symbols found.")
            logInvalidEntities(invalidSymbols)
            return invalidSymbols
        }

        return emptyList()
    }

    fun scanApplicationsAndConfigurations(
        resolver: Resolver,
        moduleList: List<KoinMetaData.Module>,
    ): List<KoinMetaData.Application> {
        val applications = scanClassApplications(resolver)
        return if (applications.isNotEmpty()){

//            logger.warn("found applications: $applications")
            // Check for default config or @ConfigScan
            val activeConfigurations = applications.flatMap { it.configurationTags }.toSet()
//            logger.warn("found configurations: $activeConfigurations")
            val configurations = extractAndBuildConfigurations(activeConfigurations, moduleList, resolver)
//            logger.logging("configurations: ${configurations.map { it.name+" -> "+it.modules.joinToString(", ") { it.packageName+"."+it.className } }}")

            // add configs content
            applications.map { application ->
                val configKeys = application.configurationTags
                application.copy(
                    configurations = configKeys.map { config -> configurations.first { it.name == config.name } }
                )
            }
        } else emptyList()
    }

    private fun extractAndBuildConfigurations(
        runningConfigurations: Set<KoinMetaData.ConfigurationTag>,
        moduleList: List<KoinMetaData.Module>,
        resolver: Resolver
    ): List<KoinMetaData.Configuration> {
        val localConfigurations = runningConfigurations.associateWith { config -> // Module to see any @Config
            moduleList.filter { if (it.configurationTags?.isNotEmpty() == true) config in it.configurationTags else false }
        }.toMutableMap()

        //check in meta modules
        val metaConfigurations = extractMetaModulesInConfigurations(resolver)

        // now associate all configs
        associateConfigurations(metaConfigurations, localConfigurations)

        return localConfigurations.mapToConfiguration()
    }

    private fun MutableMap<KoinMetaData.ConfigurationTag, List<KoinMetaData.Module>>.mapToConfiguration() : List<KoinMetaData.Configuration> {
        return map { (k,v) ->
            KoinMetaData.Configuration(
                k.name,v.map { ModuleInclude(packageName = it.packageName, className = it.name, isExpect = false, isActual = false) }
            )
        }
    }

    private fun extractMetaModulesInConfigurations(resolver: Resolver): Map<String, List<String>> {
        val metaModulesWithConfig = extractMetaModulesForConfig(resolver)

        val allMetaConfigs = metaModulesWithConfig.values.flatMap { it }
        val metaConfigurations = allMetaConfigs.associateWith { config ->
            metaModulesWithConfig.mapNotNull { (module, list) -> if (config in list) module else null }
        }
        return metaConfigurations
    }

    private fun associateConfigurations(
        metaConfigurations: Map<String, List<String>>,
        configurations: MutableMap<KoinMetaData.ConfigurationTag, List<KoinMetaData.Module>>
    ) {
        metaConfigurations.forEach { (configName, modulesName) ->
            val foundConfig = configurations[KoinMetaData.ConfigurationTag(configName)]
            if (foundConfig == null){
                logger.info("skip configuration '$configName' with $modulesName")
            }
            foundConfig?.let {
                val newList = modulesName.toSet().map { moduleName ->
                    val lastDotIndex = moduleName.lastIndexOf('.')
                    val (packageName, moduleNameOnly) = if (lastDotIndex >= 0) {
                        moduleName.substring(0, lastDotIndex) to moduleName.substring(lastDotIndex + 1)
                    } else {
                        "" to moduleName
                    }
                    KoinMetaData.Module(packageName, moduleNameOnly)
                }
                configurations[KoinMetaData.ConfigurationTag(configName)] = foundConfig + newList
            }
        }
    }

    // return Module <-> List<Config>
    private fun extractMetaModulesForConfig(resolver: Resolver): Map<String, List<String>> =
        resolver.getExternalMetaModulesSymbols().mapNotNull { metaModule ->
            val annotation =
                metaModule.annotations.firstOrNull { it.shortName.asString() == MetaModule::class.simpleName!! }
            val value = annotation?.arguments?.getValueArgument()
            value?.let {
                val configList =
                    annotation.arguments.first { it.name?.asString() == "configurations" }.value as? ArrayList<String>
                        ?: emptyList()
                // keep only modules with config
                if (configList.isNotEmpty()) {
                    Pair(
                        value,
                        configList
                    )
                } else null
            }
        }.toMap()

    fun scanKoinModulesAndDefinitions(
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
        val propertyValues: List<KoinMetaData.PropertyValue> = resolver.getValidSymbols<PropertyValue>()
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

    private fun scanClassApplications(resolver: Resolver): List<KoinMetaData.Application> {
        logger.logging("scan applications ...")
        return resolver.getValidSymbols<KoinApplication>()
            .filterIsInstance<KSClassDeclaration>()
            .map { applicationMetadataScanner.createClassApplication(it) }
            .toList()
    }

    private fun scanClassModules(resolver: Resolver): List<KoinMetaData.Module> {
        logger.logging("scan modules ...")
        return resolver.getValidSymbols<Module>()
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
                logger.warn("No module found for '$definitionPackage.${definition.label}'. Definition is added to 'defaultModule'")
            }
            foundModule.definitions.add(definition)
        } else {
            logger.logging("skip addToModule - definition(class) -> $definition -> module $foundModule - already exists")
        }
    }

    private fun logInvalidEntities(classDeclarationList: List<KSAnnotated>) {
        classDeclarationList.forEach { logger.logging("Invalid entity: $it") }
    }

    private fun Resolver.getInvalidDefinitionSymbols(): List<KSAnnotated> {
        return DEFINITION_ANNOTATION_LIST_TYPES.flatMap { annotation ->
            this.getSymbolsWithAnnotation(annotation.qualifiedName!!)
                .filter { !it.validate() }
        }
    }

    private fun Resolver.getValidDefinitionSymbols(): List<KSAnnotated> {
        return DEFINITION_ANNOTATION_LIST_TYPES.flatMap { annotation ->
            this.getSymbolsWithAnnotation(annotation.qualifiedName!!)
                .filter { it.validate() }
        }
    }

    private inline fun <reified T> Resolver.getValidSymbols(): List<KSAnnotated> {
        return this.getSymbolsWithAnnotation(T::class.qualifiedName!!)
            .filter { it.validate() }
            .toList()
    }

    private inline fun <reified T> Resolver.getInvalidSymbols(): List<KSAnnotated> {
        return this.getSymbolsWithAnnotation(T::class.qualifiedName!!)
            .filter { !it.validate() }
            .toList()
    }

    @OptIn(KspExperimental::class)
    fun Resolver.getExternalMetaModulesSymbols(): List<KSDeclaration> {
        return this.getDeclarationsFromPackage("org.koin.ksp.generated")
            .filter { a -> a.annotations.any { it.shortName.asString() == MetaModule::class.java.simpleName!! } }
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

