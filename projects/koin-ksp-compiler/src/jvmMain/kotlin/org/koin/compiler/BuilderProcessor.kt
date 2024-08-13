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
import org.koin.compiler.KspOptions.*
import org.koin.compiler.generator.KoinGenerator
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.scanner.KoinMetaDataScanner
import org.koin.compiler.verify.KoinConfigChecker
import org.koin.compiler.verify.KoinTagWriter

class BuilderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val koinCodeGenerator = KoinGenerator(codeGenerator, logger, isComposeViewModelActive() || isKoinComposeViewModelActive())
    private val koinMetaDataScanner = KoinMetaDataScanner(logger)
    private val koinTagWriter = KoinTagWriter(codeGenerator,logger)
    private val koinConfigChecker = KoinConfigChecker(codeGenerator, logger, koinTagWriter)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.logging("Scan symbols ...")

        val invalidSymbols = koinMetaDataScanner.scanSymbols(resolver)
        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid symbols found (${invalidSymbols.size}), waiting for next round")
            return invalidSymbols
        }

        val defaultModule = KoinMetaData.Module(
            packageName = "",
            name = "defaultModule",
            isDefault = true,
        )

        logger.logging("Scan metadata ...")
        val moduleList = koinMetaDataScanner.scanKoinModules(defaultModule)

        logger.logging("Generate code ...")
        koinCodeGenerator.generateModules(moduleList, defaultModule, isDefaultModuleActive())

        if (isConfigCheckActive()) {
            logger.warn("Koin Configuration Check")
            koinConfigChecker.verifyDefinitionDeclarations(moduleList + defaultModule, resolver)
            koinConfigChecker.verifyModuleIncludes(moduleList + defaultModule, resolver)
        }
        return emptyList()
    }

    private fun isConfigCheckActive(): Boolean {
        return options.getOrDefault(KOIN_CONFIG_CHECK.name, "false") == true.toString()
    }

    //TODO Use Koin 4.0 ViewModel DSL
    @Deprecated("use isKoinComposeViewModelActive")
    private fun isComposeViewModelActive(): Boolean {
        logger.warn("[Deprecated] Please use KOIN_USE_COMPOSE_VIEWMODEL arg")
        return options.getOrDefault(USE_COMPOSE_VIEWMODEL.name, "false") == true.toString()
    }

    private fun isKoinComposeViewModelActive(): Boolean {
        logger.warn("Use Compose ViewModel for @KoinViewModel generation")
        return options.getOrDefault(KOIN_USE_COMPOSE_VIEWMODEL.name, "false") == true.toString()
    }

    //TODO turn KOIN_DEFAULT_MODULE to false by default - Next Major version (breaking)
    private fun isDefaultModuleActive(): Boolean {
        return options.getOrDefault(KOIN_DEFAULT_MODULE.name, "true") == true.toString()
    }
}


class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.logger, environment.options)
    }
}