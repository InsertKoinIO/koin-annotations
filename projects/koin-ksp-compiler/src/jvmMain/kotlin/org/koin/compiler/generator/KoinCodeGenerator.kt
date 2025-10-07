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

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.camelCase
import org.koin.compiler.metadata.tag.TagResolver

class KoinCodeGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    //TODO Remove isComposeViewModelActive with Koin 4
    val isViewModelMPActive: Boolean,
    val tagResolver: TagResolver
) {
    init {
        LOGGER = logger
    }

    fun generateModules(
        moduleList: List<KoinMetaData.Module>,
        defaultModule: KoinMetaData.Module,
        generateDefaultModule: Boolean,
        doExportDefinitions: Boolean
    ) {
        logger.info("generate ${moduleList.size} modules ...")
        
        // Pre-compute batch tag existence checks for all modules and definitions
        val allDefinitions = moduleList.flatMap { it.definitions } + defaultModule.definitions
        tagResolver.batchCheckTagsExist(moduleList, allDefinitions, emptyList())
        
        // Batch check already generated status for all modules
        moduleList.forEach { module ->
            if (module.alreadyGenerated == null) {
                module.alreadyGenerated = tagResolver.tagExists(module)
            }
        }
        
        moduleList.forEach {
            val isActualWithLocalDefinitions = it.isActual && it.definitions.all { it.isActual.not() }
            val isNotActual = it.isActual.not()
            if (isNotActual || isActualWithLocalDefinitions) generateModule(it)
        }

        if (defaultModule.definitions.isNotEmpty()) {
            generateDefaultFile(defaultModule, generateDefaultModule, doExportDefinitions)
        }
    }

    private fun generateDefaultFile(
        defaultModule: KoinMetaData.Module,
        generateDefaultModule: Boolean,
        doExportDefinitions: Boolean
    ) {
        logger.info("generate default file ...")

        checkAlreadyGenerated(defaultModule)
        val hasDefaultDefinitions = defaultModule.definitions.any { !tagResolver.tagExists(it) }

        if (defaultModule.alreadyGenerated == false && hasDefaultDefinitions){
            if (generateDefaultModule && defaultModule.definitions.isNotEmpty()) {
                LOGGER.warn("Generating 'defaultModule' with ${defaultModule.definitions.size} definitions")
            }
            defaultModule.setCurrentDefinitionsToExternals()
            DefaultModuleWriter(codeGenerator, tagResolver, defaultModule, generateDefaultModule, doExportDefinitions).writeModule(isViewModelMPActive)
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.logging("generate module ${module.name}")

        checkAlreadyGenerated(module)

        if (module.alreadyGenerated == false){
            val definitionsCount = module.definitions.size
            if (definitionsCount > MAX_MODULE_DEFINITIONS) {
                generateSplitModule(module, definitionsCount)
            } else {
                ClassModuleWriter(codeGenerator, tagResolver, module).writeModule(isViewModelMPActive)
            }
        }
    }

    private fun checkAlreadyGenerated(module: KoinMetaData.Module){
        if (module.alreadyGenerated == null){
            module.alreadyGenerated = tagResolver.tagExists(module)
        }
    }

    private fun generateSplitModule(module: KoinMetaData.Module, definitionsCount: Int) {
        val splitCount = definitionsCount / MAX_MODULE_DEFINITIONS
        logger.warn("Module '${module.name}' is exceeding $MAX_MODULE_DEFINITIONS definitions ($definitionsCount found). We need to split generation into ${splitCount + 1} modules ...")
        
        val moduleBaseName = module.packageName.camelCase() + module.name.replaceFirstChar { it.uppercase() }
        
        // Generate submodules with memory-efficient chunking
        val subModulesInclude = mutableListOf<KoinMetaData.ModuleInclude>()
        
        // Process definitions in chunks without creating intermediate collections
        module.definitions.chunked(MAX_MODULE_DEFINITIONS).forEachIndexed { index, definitionChunk ->
            val subModuleName = "${moduleBaseName}$index"
            val subModule = module.copy(
                includes = emptyList(),
                definitions = definitionChunk.toMutableList(),
                externalDefinitions = mutableListOf(),
                packageName = "",
                name = subModuleName,
                isSplit = true
            )
            
            // Generate the submodule immediately to reduce memory pressure
            ClassModuleWriter(codeGenerator, tagResolver, subModule).writeModule(isViewModelMPActive)
            
            // Add to includes for main module
            subModulesInclude.add(
                KoinMetaData.ModuleInclude(
                    subModule.packageName,
                    subModule.name,
                    subModule.isExpect,
                    subModule.isActual
                )
            )
        }
        
        // Generate main module with includes
        val mainModule = module.copy(
            includes = (module.includes ?: mutableListOf()) + subModulesInclude,
            definitions = mutableListOf() // Clear definitions to avoid duplication
        )
        ClassModuleWriter(codeGenerator, tagResolver, mainModule).writeModule(isViewModelMPActive, subModulesInclude)
    }

    fun generateApplications(applications: List<KoinMetaData.Application>) {
        applications.forEach { application ->
            if (application.alreadyGenerated == null){
                application.alreadyGenerated = tagResolver.tagExists(application)
            }

            if (application.alreadyGenerated == false){
                ApplicationClassWriter(codeGenerator,application).writeApplication()
            }
        }
    }

    companion object {
        lateinit var LOGGER: KSPLogger
            private set
    }
}

const val MAX_MODULE_DEFINITIONS = 500
