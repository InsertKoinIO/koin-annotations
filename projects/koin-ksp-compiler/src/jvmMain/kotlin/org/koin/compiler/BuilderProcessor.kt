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
import org.koin.compiler.generator.KoinCodeGenerator
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.scanner.KoinMetaDataScanner
import org.koin.compiler.verify.KoinConfigChecker
import org.koin.compiler.verify.KoinTagWriter

class BuilderProcessor(
    codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val isComposeViewModelActive = isComposeViewModelActive() || isKoinComposeViewModelActive()
    private val koinCodeGenerator = KoinCodeGenerator(codeGenerator, logger, isComposeViewModelActive)
    private val koinMetaDataScanner = KoinMetaDataScanner(logger)
    private val koinTagWriter = KoinTagWriter(codeGenerator,logger)
    private val koinConfigChecker = KoinConfigChecker(codeGenerator, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        initComponents(resolver)

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

        logger.logging("Build metadata ...")
        val moduleList = koinMetaDataScanner.scanKoinModules(defaultModule)

        logger.logging("Generate code ...")
        koinCodeGenerator.generateModules(moduleList, defaultModule, isDefaultModuleActive())

        val allModules = moduleList + defaultModule
        koinTagWriter.writeAllTags(moduleList, defaultModule)

        if (isConfigCheckActive()) {
            logger.warn("Check Configuration ...")
            koinConfigChecker.verifyDefinitionDeclarations(allModules, resolver)
            koinConfigChecker.verifyModuleIncludes(allModules, resolver)
        }
        return emptyList()
    }

    private fun initComponents(resolver: Resolver) {
        koinCodeGenerator.resolver = resolver
        koinTagWriter.resolver = resolver
    }

    private fun isConfigCheckActive(): Boolean {
        return options.getOrDefault(KOIN_CONFIG_CHECK.name, "false") == true.toString()
    }

    //TODO Use Koin 4.0 ViewModel DSL
    @Deprecated("use isKoinComposeViewModelActive")
    private fun isComposeViewModelActive(): Boolean {
        val option = options.getOrDefault(USE_COMPOSE_VIEWMODEL.name, "false") == true.toString()
        if (option) logger.warn("[Deprecated] 'USE_COMPOSE_VIEWMODEL' arg is deprecated. Please use 'KOIN_USE_COMPOSE_VIEWMODEL'")
        return option
    }

    private fun isKoinComposeViewModelActive(): Boolean {
        val option =
            options.getOrDefault(KOIN_USE_COMPOSE_VIEWMODEL.name, "false") == true.toString()
        if (option) logger.warn("Activate Compose ViewModel for @KoinViewModel generation")
        return option
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