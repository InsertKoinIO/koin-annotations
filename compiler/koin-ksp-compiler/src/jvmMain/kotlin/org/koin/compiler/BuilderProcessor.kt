/*
 * Copyright 2017-2023 the original author or authors.
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

import appendText
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import org.koin.compiler.generator.KoinGenerator
import org.koin.compiler.generator.getFile
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.scanner.KoinMetaDataScanner

class BuilderProcessor(
    codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val koinCodeGenerator = KoinGenerator(codeGenerator, logger)
    private val koinMetaDataScanner = KoinMetaDataScanner(logger)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.logging("Scanning symbols ...")
        val invalidSymbols = koinMetaDataScanner.scanSymbols(resolver)
        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid symbols found (${invalidSymbols.size}), waiting for next round")
            return invalidSymbols
        }

        val defaultModule = KoinMetaData.Module(
            packageName = "",
            name = "defaultModule"
        )

        logger.logging("Scan metadata ...")
        val moduleList = koinMetaDataScanner.extractKoinMetaData(defaultModule)

        logger.logging("Generate code ...")
        koinCodeGenerator.generateModules(moduleList, defaultModule)

        //TODO Use argument here to activate this part
        val genSize = koinCodeGenerator.codeGenerator.generatedFile.size

        val ignored = listOf("kotlin.Lazy", "kotlin.Any")
        (moduleList + defaultModule).map { module ->
            module.definitions.forEach { def ->
                val label = def.label
                val cn = label.first().toString().toUpperCase() + label.takeLast(label.length - 1)
                val file = koinCodeGenerator.codeGenerator.getFile(fileName = cn)
                file.appendText("package org.koin.ksp.generated")
                def.bindings.forEach { d ->
                    val cn = d.simpleName.asString()
                    file.appendText("\nclass KoinDef$cn")
                }
                if (genSize == 0) {
                    def.parameters.forEach { param ->
                        if (param is KoinMetaData.ConstructorParameter.Dependency) {
                            val p = param.type.declaration.qualifiedName?.asString()
//                            logger.warn("$label look at dependency => $p")
                            if (p !in ignored && p != null) {
                                val d =
                                    resolver.getKSNameFromString("org.koin.ksp.generated.KoinDef" + param.type.declaration.simpleName.asString())
//                                logger.warn("ksn => ${d.asString()}")
                                val dc = resolver.getClassDeclarationByName(d)
                                if (dc != null) {
//                                    logger.warn("ks => $it")
                                } else {
                                    logger.error("$label need dependency type '$p', but is not found. Check your Koin configuration to add the right definition or type binding.")
                                }
                            }
                        }
                    }
                }
            }
        }
        return emptyList()
    }
}

class BuilderProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return BuilderProcessor(environment.codeGenerator, environment.logger)
    }
}
