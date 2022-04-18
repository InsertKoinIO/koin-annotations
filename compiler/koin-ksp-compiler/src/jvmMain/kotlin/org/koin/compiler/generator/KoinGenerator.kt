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
package org.koin.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import generateClassModule
import generateDefaultModuleFooter
import generateDefaultModuleForDefinitions
import generateDefaultModuleHeader
import generateFieldDefaultModule
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.scanner.ModuleMap
import java.io.OutputStream

class KoinGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) {

    init {
        LOGGER = logger
    }

    fun generateModules(
        moduleMap: ModuleMap,
        defaultModule: KoinMetaData.Module
    ) {
        val modules = moduleMap.values.flatten()
        if (modules.isEmpty()) {
            logger.logging("no modules were found. skip ...")
            return
        }

        fun withGeneratingDefaultModule(block: () -> Unit) {
            if (defaultModule.definitions.isNotEmpty()){
                codeGenerator.getDefaultFile().generateDefaultModuleHeader()
            }
            block()
            generateModule(defaultModule)
            if (defaultModule.definitions.isNotEmpty()) {
                codeGenerator.getDefaultFile().generateDefaultModuleFooter()
            }
        }

        logger.logging("generate ${modules.size} modules ...")
        withGeneratingDefaultModule {
            modules.forEach { module -> generateModule(module) }
        }
    }

    private fun generateModule(module: KoinMetaData.Module) {
        logger.logging("generate $module - ${module.type}")
        codeGenerator.getDefaultFile().let { defaultFile ->
            if (module.definitions.isNotEmpty()) {
                when (module.type) {
                    KoinMetaData.ModuleType.FIELD -> {
                        defaultFile.generateFieldDefaultModule(module.definitions)
                    }
                    KoinMetaData.ModuleType.CLASS -> {
                        val moduleFile = codeGenerator.getFile(fileName = "${module.name}Gen")
                        generateClassModule(moduleFile, module)
                    }
                }
            } else {
                logger.logging("no definition for $module")
            }
        }
    }

    fun generateDefaultModule(
        definitions: List<KoinMetaData.Definition>
    ) {
        generateDefaultModuleForDefinitions(definitions)
    }

    companion object {
        lateinit var LOGGER: KSPLogger
            private set
    }
}

private var defaultFile : OutputStream? = null
fun CodeGenerator.getDefaultFile() : OutputStream {
    return if (defaultFile != null) defaultFile!!
    else {
        defaultFile = createNewFile(
            Dependencies.ALL_FILES,
            "org.koin.ksp.generated",
            "Default"
        )
        defaultFile!!
    }
}

fun CodeGenerator.getFile(packageName: String = "org.koin.ksp.generated", fileName: String) = createNewFile(
    Dependencies.ALL_FILES,
    packageName,
    fileName
)