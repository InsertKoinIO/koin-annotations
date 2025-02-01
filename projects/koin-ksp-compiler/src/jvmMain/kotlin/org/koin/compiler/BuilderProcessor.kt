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
import org.koin.compiler.metadata.KoinTagWriter
import org.koin.compiler.scanner.KoinMetaDataScanner
import org.koin.compiler.scanner.KoinTagMetaDataScanner
import org.koin.compiler.verify.KoinConfigChecker
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.markNow

class BuilderProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val isViewModelMPActive = isKoinViewModelMPActive()
    private val koinCodeGenerator = KoinCodeGenerator(codeGenerator, logger, isViewModelMPActive)
    private val koinMetaDataScanner = KoinMetaDataScanner(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        initComponents(resolver)

        val doLogTimes = doLogTimes()

        val mainTime = if (doLogTimes) markNow() else null
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

        if (doLogTimes && mainTime != null) {
            mainTime.elapsedNow()
            logger.warn("Koin Configuration Generated in ${mainTime.elapsedNow()}")
        }

        val isConfigCheckActive = isConfigCheckActive()
        if (isConfigCheckActive) {
            logger.warn("Koin Configuration Check ...")
            val checkTime = if (doLogTimes) markNow() else null

            // Tags are used to verify generated content (KMP)
            KoinTagWriter(codeGenerator, logger, resolver, isConfigCheckActive)
                .writeAllTags(moduleList, defaultModule)

            val isAlreadyGenerated = codeGenerator.generatedFile.isEmpty()
            if (!isAlreadyGenerated) {
                warnConfigCheckDone(doLogTimes, checkTime)
                return emptyList()
            }

            val metaTagScanner = KoinTagMetaDataScanner(logger, resolver)
            val invalidsMetaSymbols = metaTagScanner.findInvalidSymbols()
            if (invalidsMetaSymbols.isNotEmpty()) {
                logger.logging("Invalid symbols found (${invalidsMetaSymbols.size}), waiting for next round")
                return invalidSymbols
            }

            val checker = KoinConfigChecker(logger, resolver)
            checker.verifyMetaModules(metaTagScanner.findMetaModules())
            checker.verifyMetaDefinitions(metaTagScanner.findMetaDefinitions())

            warnConfigCheckDone(doLogTimes, checkTime)
        }
        return emptyList()
    }

    private fun initComponents(resolver: Resolver) {
        koinCodeGenerator.resolver = resolver
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

    private fun doLogTimes(): Boolean {
        return options.getOrDefault(KOIN_LOG_TIMES.name, "false") == true.toString()
    }

    private fun warnConfigCheckDone(
        doLogTimes: Boolean,
        checkTime: TimeSource.Monotonic.ValueTimeMark?
    ) {
        if (doLogTimes && checkTime != null) {
            checkTime.elapsedNow()
            logger.warn("Koin Configuration Check done in ${checkTime.elapsedNow()}")
        }
    }
}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.logger, environment.options)
    }
}
