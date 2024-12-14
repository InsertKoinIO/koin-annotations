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
import org.koin.compiler.verify.ext.getResolution

class KoinCodeGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    //TODO Remove isComposeViewModelActive with Koin 4
    val isComposeViewModelActive: Boolean
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
        logger.logging("generate ${moduleList.size} modules ...")
        moduleList.forEach { generateModule(it) }

        if (defaultModule.definitions.isNotEmpty()) {
            generateDefaultFile(defaultModule, generateDefaultModule)
        }
    }

    private fun generateDefaultFile(
        defaultModule: KoinMetaData.Module,
        generateDefaultModule: Boolean
    ) {
        logger.logging("generate default file ...")

        checkAlreadyGenerated(defaultModule)
        val hasDefaultDefinitions = defaultModule.definitions.any { resolver.getResolution(it) == null }

        if (defaultModule.alreadyGenerated == false && hasDefaultDefinitions){
            defaultModule.setCurrentDefinitionsToExternals()
            DefaultModuleWriter(codeGenerator, resolver, defaultModule, generateDefaultModule).writeModule(isComposeViewModelActive)
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.logging("generate module ${module.name}")

        checkAlreadyGenerated(module)

        if (module.alreadyGenerated == false && !module.isExpect){
            ClassModuleWriter(codeGenerator, resolver, module).writeModule(isComposeViewModelActive)
        }
    }

    private fun checkAlreadyGenerated(module: KoinMetaData.Module){
        if (module.alreadyGenerated == null){
            module.alreadyGenerated = resolver.getResolution(module) != null
        }
    }

    companion object {
        lateinit var LOGGER: KSPLogger
            private set
    }
}

