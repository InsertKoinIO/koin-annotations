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
import org.koin.compiler.generator.GenerationConfig
import org.koin.compiler.generator.KoinCodeGenerator
import org.koin.compiler.generator.generateProxies
import org.koin.compiler.metadata.KOIN_VIEWMODEL
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.tag.KoinTagWriter
import org.koin.compiler.metadata.tag.TagResolver
import org.koin.compiler.scanner.KoinMetaDataScanner
import org.koin.compiler.scanner.KoinTagMetaDataScanner
import org.koin.compiler.verify.KoinConfigChecker
import kotlin.time.TimeSource.Monotonic.markNow

class BuilderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val tagResolver = TagResolver()
    private val isViewModelMPActive = isKoinViewModelMPActive()
    private val koinCodeGenerator = KoinCodeGenerator(codeGenerator, logger, isViewModelMPActive, tagResolver)
    private val koinMetaDataScanner = KoinMetaDataScanner(logger)
    private val metaTagScanner = KoinTagMetaDataScanner(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        initComponents(resolver)

        val doLogTimes = doLogTimes()

        GenerationConfig.setGenerationPath(getCustomGenerationPackage())

        val mainTime = if (doLogTimes) markNow() else null
        logger.warn("Scan symbols hello local...")

        // DEBUG: Check all files KSP can see
        val allModuleAnnotations = resolver.getSymbolsWithAnnotation("org.koin.core.annotation.Module").toList()
        logger.warn("DEBUG BuilderProcessor: Resolver found ${allModuleAnnotations.size} symbols with @Module")
        allModuleAnnotations.forEach {
            logger.warn("DEBUG BuilderProcessor: @Module symbol: ${it}")
        }

        val (invalidModuleSymbols, invalidDefinitionSymbols) = koinMetaDataScanner.findInvalidSymbolsSeparately(resolver)

        // If modules themselves are invalid, we must wait
        if (invalidModuleSymbols.isNotEmpty()) {
            logger.warn("Invalid @Module symbols found (${invalidModuleSymbols.size}), waiting for next round")
            invalidModuleSymbols.forEach {
                logger.warn("Invalid @Module symbol: $it")
            }
            return invalidModuleSymbols + invalidDefinitionSymbols
        }

        val defaultModule = KoinMetaData.Module(
            packageName = "",
            name = "defaultModule",
            isDefault = true,
        )

        logger.warn("Build metadata ...")
        val moduleList = koinMetaDataScanner.scanKoinModulesAndDefinitions(
            defaultModule,
            resolver
        )

        // If we have invalid definitions but valid modules, process modules and return invalid definitions
        if (invalidDefinitionSymbols.isNotEmpty()) {
            logger.warn("Invalid definition symbols found (${invalidDefinitionSymbols.size}), but processing valid modules anyway")
            invalidDefinitionSymbols.forEach {
                logger.warn("Invalid definition symbol: $it")
            }
        }
        val applications = koinMetaDataScanner.scanApplicationsAndConfigurations(
            resolver,
            moduleList
        )

        val doExportDefinitions = doExportDefinitions()
        if (!doExportDefinitions && defaultModule.definitions.isNotEmpty()){
            logger.warn("Skip definitions export ...")
            defaultModule.definitions.clear()
        }

        val monitoredDefinitions = (moduleList+defaultModule).flatMap { it.definitions }.filter { it.isMonitored }.filterIsInstance<KoinMetaData.Definition.ClassDefinition>()
        koinCodeGenerator.generateProxies(monitoredDefinitions)

        logger.warn("Generate code ...")
        koinCodeGenerator.generateModules(moduleList, defaultModule, isDefaultModuleActive(), doExportDefinitions)
        koinCodeGenerator.generateApplications(applications)

        val isConfigCheckActive = isConfigCheckActive()

        // Only write tags if all symbols are valid (no invalid definitions)
        // This prevents duplicate tag generation across multiple KSP rounds
        if (invalidDefinitionSymbols.isEmpty()) {
            logger.warn("Writing tags for all components...")
            // Tags are used to verify generated content (KMP)
            // Pre-compute batch tag existence for all components before writing
            val allDefinitions = moduleList.flatMap { it.definitions } + defaultModule.definitions
            tagResolver.batchCheckTagsExist(moduleList, allDefinitions, applications)

            KoinTagWriter(codeGenerator, logger, tagResolver)
                .writeAllTags(moduleList, defaultModule, applications)
        } else {
            logger.warn("Skipping tag generation due to invalid definitions - will write in next round")
        }

        if (doLogTimes && mainTime != null) {
            mainTime.elapsedNow()
            logger.warn("Koin Configuration Generated in ${mainTime.elapsedNow()} (LOCAL)")
        }

        val isAlreadyGenerated = codeGenerator.generatedFile.isEmpty()

        if(isDefaultModuleActive() && !isAlreadyGenerated) {
            logger.warn("[Deprecation] 'defaultModule' generation is deprecated. Use KSP argument arg(\"KOIN_DEFAULT_MODULE\",\"true\") to activate default module generation.")
        }

        //TODO Configuration check is associated to a configuration & modules
        if (isConfigCheckActive && isAlreadyGenerated) {
            logger.warn("Koin Configuration Check ...")
            val checkTime = if (doLogTimes) markNow() else null

            val invalidsMetaSymbols = metaTagScanner.findInvalidSymbols()
            if (invalidsMetaSymbols.isNotEmpty()) {
                logger.warn("Invalid meta symbols found (${invalidsMetaSymbols.size}), waiting for next round")
                return invalidsMetaSymbols + invalidDefinitionSymbols
            }

            KoinConfigChecker(logger, tagResolver).apply {
                verify(
                    metaTagScanner.findMetaModules(),
                    metaTagScanner.findMetaDefinitions()
                    )
            }

            if (doLogTimes && checkTime != null) {
                checkTime.elapsedNow()
                logger.warn("Koin Configuration Check done in ${checkTime.elapsedNow()}")
            }
        }
        // Return invalid definition symbols so they can be processed in the next round
        return invalidDefinitionSymbols
    }

    private fun initComponents(resolver: Resolver) {
        tagResolver.resolver = resolver
        metaTagScanner.resolver = resolver
    }

    private fun isConfigCheckActive(): Boolean {
        return options.getOrDefault(KOIN_CONFIG_CHECK.name, "false") == true.toString()
    }

    // Allow to disable usage of ViewModel MP API and
    private fun isKoinViewModelMPActive(): Boolean {
        val option = options.getOrDefault(KOIN_USE_COMPOSE_VIEWMODEL.name, "true") == true.toString()
        if (!option) {
            logger.warn("[Deprecation] 'KOIN_USE_COMPOSE_VIEWMODEL' is now enabled by default. Please use 'KOIN_USE_COMPOSE_VIEWMODEL' = true, to activate latest ViewModel API with '${KOIN_VIEWMODEL.import}'")
        }
        return option
    }

    private fun isDefaultModuleActive(): Boolean {
        return options.getOrDefault(KOIN_DEFAULT_MODULE.name, "false") == true.toString()
    }

    private fun doLogTimes(): Boolean {
        return options.getOrDefault(KOIN_LOG_TIMES.name, "false") == true.toString()
    }

    private fun getCustomGenerationPackage(): String? {
        return options.getOrDefault(KOIN_GENERATION_PACKAGE.name, null)
    }

    private fun doExportDefinitions(): Boolean {
        return options.getOrDefault(KOIN_EXPORT_DEFINITIONS.name, "true") == true.toString()
    }
}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.logger, environment.options)
    }
}
