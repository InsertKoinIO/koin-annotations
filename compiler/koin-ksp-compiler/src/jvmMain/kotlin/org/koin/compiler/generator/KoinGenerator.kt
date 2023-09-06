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
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import generateClassModule
import generateDefaultModuleFooter
import generateDefaultModuleHeader
import generateFieldDefaultModule
import org.koin.compiler.metadata.KoinMetaData
import java.io.OutputStream

class KoinGenerator(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) {

    init {
        LOGGER = logger
    }

    fun generateModules(
        moduleList: List<KoinMetaData.Module>,
        defaultModule: KoinMetaData.Module
    ) {
        logger.logging("generate ${moduleList.size} modules ...")
        moduleList.forEach { generateModule(it) }

        if (defaultModule.definitions.isNotEmpty()) {
            logger.logging("generate default module ...")
            val defaultModuleFile = codeGenerator.getFile(fileName = "Default")
            defaultModuleFile.generateDefaultModuleHeader(defaultModule.definitions)
            generateModule(defaultModule, defaultModuleFile)
            defaultModuleFile.generateDefaultModuleFooter()
            defaultModuleFile.close()
        }
    }

    private fun generateModule(module: KoinMetaData.Module, defaultFile: OutputStream? = null) {
        logger.logging("generate $module - ${module.type}")

        val isFieldModule = module.type == KoinMetaData.ModuleType.FIELD && module.definitions.isNotEmpty()
        if (isFieldModule){
            // generate field module
            defaultFile!!.generateFieldDefaultModule(module.definitions)
        } else {
            // generate class module
            val moduleFile = codeGenerator.getFile(fileName = module.generateModuleFileName())
            generateClassModule(moduleFile, module)
        }
    }

    private fun KoinMetaData.Module.generateModuleFileName(): String {
        val extensionName = packageName("$")
        return "${name}Gen${extensionName}"
    }

    companion object {
        lateinit var LOGGER: KSPLogger
            private set
    }
}

fun CodeGenerator.getFile(packageName: String = "org.koin.ksp.generated", fileName: String): OutputStream {
    return try {
        createNewFile(
            Dependencies.ALL_FILES,
            packageName,
            fileName
        )
    } catch (ex: FileAlreadyExistsException){
        ex.file.outputStream()
    }
}
