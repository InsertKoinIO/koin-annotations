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
import com.google.devtools.ksp.processing.Resolver
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.camelCase
import org.koin.compiler.resolver.getResolution

class KoinCodeGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    //TODO Remove isComposeViewModelActive with Koin 4
    val isViewModelMPActive: Boolean
) {
    lateinit var resolver: Resolver

    init {
        LOGGER = logger
    }

    fun generateModules(
        moduleList: List<KoinMetaData.Module>,
        defaultModule: KoinMetaData.Module,
        generateDefaultModule : Boolean
    ) {
        logger.info("generate ${moduleList.size} modules ...")
        moduleList.forEach {
            val isActualWithLocalDefinitions = it.isActual && it.definitions.all { it.isActual.not() }
            val isNotActual = it.isActual.not()
            if (isNotActual || isActualWithLocalDefinitions) generateModule(it)
        }

        if (defaultModule.definitions.isNotEmpty()) {
            generateDefaultFile(defaultModule, generateDefaultModule)
        }
    }

    private fun generateDefaultFile(
        defaultModule: KoinMetaData.Module,
        generateDefaultModule: Boolean
    ) {
        logger.info("generate default file ...")

        checkAlreadyGenerated(defaultModule)
        val hasDefaultDefinitions = defaultModule.definitions.any { resolver.getResolution(it) == null }

        if (defaultModule.alreadyGenerated == false && hasDefaultDefinitions){
            if (generateDefaultModule && defaultModule.definitions.isNotEmpty()) {
                LOGGER.warn("Generating 'defaultModule' with ${defaultModule.definitions.size} definitions")
            }
            defaultModule.setCurrentDefinitionsToExternals()
            DefaultModuleWriter(codeGenerator, resolver, defaultModule, generateDefaultModule).writeModule(isViewModelMPActive)
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.logging("generate module ${module.name}")

        checkAlreadyGenerated(module)

        if (module.alreadyGenerated == false){

            val definitionsCount = module.definitions.size
            if (definitionsCount > MAX_MODULE_DEFINITIONS) {
                val splitCount = definitionsCount / MAX_MODULE_DEFINITIONS
                logger.warn("Module '${module.name}' is exceeding $MAX_MODULE_DEFINITIONS definitions ($definitionsCount found). We need to split generation into ${splitCount +1} modules ...")
                // Create one main module to include sub generate modules
                val subModules : List<KoinMetaData.Module> = module.definitions.chunked(MAX_MODULE_DEFINITIONS).mapIndexed { index, list ->
                    module.copy(includes = emptyList(), definitions = list.toMutableList(), externalDefinitions = mutableListOf(), packageName = "", name = module.packageName.camelCase()+module.name.capitalize()+index, isSplit = true)
                }
                val subModulesInclude : List<KoinMetaData.ModuleInclude> = subModules.map { m ->
                    KoinMetaData.ModuleInclude(m.packageName, m.name, m.isExpect, m.isActual)
                }
                val main = module.copy(includes = (module.includes ?: mutableListOf()) + subModulesInclude, definitions = mutableListOf()) // keep externalDefinitions

                val allModules = (subModules + main)
                allModules.mapIndexed { index, m ->
                    val generateIncludes = if (index == allModules.indexOf(main)) subModulesInclude else emptyList()
                    ClassModuleWriter(codeGenerator, resolver, m).writeModule(isViewModelMPActive, generateIncludes)
                }
            } else {
                ClassModuleWriter(codeGenerator, resolver, module).writeModule(isViewModelMPActive)
            }
        }
    }

    private fun checkAlreadyGenerated(module: KoinMetaData.Module){
        if (module.alreadyGenerated == null){
            module.alreadyGenerated = resolver.getResolution(module) != null
        }
    }

    fun generateApplications(applications: List<KoinMetaData.Application>) {

//        logger.logging("generating applications ${applications.size} ...")
        applications.forEach { application ->
            ApplicationClassWriter(codeGenerator,resolver,application).writeApplication()
        }
    }

    companion object {
        lateinit var LOGGER: KSPLogger
            private set
    }
}

const val MAX_MODULE_DEFINITIONS = 500
