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
package org.koin.compiler

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import org.koin.compiler.KspOptions.KOIN_CONFIG_CHECK
import org.koin.compiler.generator.KoinGenerator
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.scanner.KoinMetaDataScanner
import org.koin.compiler.verify.KoinConfigVerification

class BuilderProcessor(
    codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val koinCodeGenerator = KoinGenerator(codeGenerator, logger)
    private val koinMetaDataScanner = KoinMetaDataScanner(logger)
    private val koinConfigVerification = KoinConfigVerification(codeGenerator, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.logging("Scanning symbols ...")

        //TODO Handle allowDefaultModule option

        val invalidSymbols = koinMetaDataScanner.scanSymbols(resolver)
        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid symbols found (${invalidSymbols.size}), waiting for next round")
            return invalidSymbols
        }

        val defaultModule = KoinMetaData.Module(
            packageName = "",
            name = "defaultModule",
            isDefault = true
        )

        logger.logging("Scan metadata ...")
        val moduleList = koinMetaDataScanner.extractKoinMetaData(defaultModule)

        if (isDefaultModuleDisabled()){
            if (defaultModule.definitions.isNotEmpty()){
                logger.error("Default module is disabled!")
                defaultModule.definitions.forEach { def ->
                    logger.error("definition '${def.packageName}.${def.label}' needs to be defined in a module")
                }
            }
        }

        logger.logging("Generate code ...")
        koinCodeGenerator.generateModules(moduleList, defaultModule)

        if (isConfigCheckActive()) {
            logger.warn("[Experimental] Koin Configuration Check")
            koinConfigVerification.verifyDefinitionDeclarations(moduleList + defaultModule, resolver)
            koinConfigVerification.verifyModuleIncludes(moduleList + defaultModule, resolver)
        }
        return emptyList()
    }

    private fun isConfigCheckActive(): Boolean {
        return options[KOIN_CONFIG_CHECK.name] == true.toString()
    }

    private fun isDefaultModuleDisabled(): Boolean {
        return options[KspOptions.KOIN_DEFAULT_MODULE.name] == false.toString()
    }
}


class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.logger, environment.options)
    }
}