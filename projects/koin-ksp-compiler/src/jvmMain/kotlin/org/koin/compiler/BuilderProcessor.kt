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
import org.koin.compiler.metadata.KoinTagWriter

class BuilderProcessor(
    codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val isViewModelMPActive = isKoinViewModelMPActive()
    private val koinCodeGenerator = KoinCodeGenerator(codeGenerator, logger, isViewModelMPActive)
    private val koinMetaDataScanner = KoinMetaDataScanner(logger)
    private val koinTagWriter = KoinTagWriter(codeGenerator, logger)
    private val koinConfigChecker = KoinConfigChecker(codeGenerator, logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        initComponents(resolver)

        logger.logging("Scan symbols ...")

        val invalidSymbols = koinMetaDataScanner.findInvalidSymbols(resolver)
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
        val moduleList = koinMetaDataScanner.scanKoinModules(
            defaultModule,
            resolver
        )

        logger.logging("Generate code ...")
        koinCodeGenerator.generateModules(moduleList, defaultModule, isDefaultModuleActive())

        val isConfigCheckActive = isConfigCheckActive()

        // Tags are used to verify generated content (KMP)
        koinTagWriter.writeAllTags(moduleList, defaultModule, isConfigCheckActive)

        if (isConfigCheckActive) {
            logger.warn("Check Configuration ...")

//            val moduleList = koinMetaDataScanner.scanKoinModules(
//                defaultModule,
//                resolver
//            )
            val allModules = moduleList + defaultModule
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

    // Allow to disable usage of ViewModel MP API and
    private fun isKoinViewModelMPActive(): Boolean {
        val option = options.getOrDefault(KOIN_USE_COMPOSE_VIEWMODEL.name, "true") == true.toString()
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
