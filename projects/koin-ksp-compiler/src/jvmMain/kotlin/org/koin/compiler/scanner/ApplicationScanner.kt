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
package org.koin.compiler.scanner

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import org.koin.compiler.metadata.*
import org.koin.compiler.scanner.ModuleScanner.Companion.toModuleIncludes
import org.koin.compiler.scanner.ext.*
import org.koin.core.annotation.KoinApplication

class ApplicationScanner(
    val logger: KSPLogger
) : FunctionScanner(isModuleFunction = true) {

    fun createClassApplication(element: KSAnnotated): KoinMetaData.Application {
        val declaration = (element as KSClassDeclaration)
        val modulePackage = declaration.getPackageName().filterForbiddenKeywords()

        val annotations = declaration.annotations
        val configurations = getConfigurationScans(annotations)
        val includes = getIncludes(annotations)

        val name = "$element"
        val type = if (declaration.classKind == ClassKind.OBJECT) {
            KoinMetaData.ModuleType.OBJECT
        } else  {
            KoinMetaData.ModuleType.CLASS
        }

        val applicationMetadata = KoinMetaData.Application(
            packageName = modulePackage,
            name = name,
            type = type,
            configurationTags = configurations ?: defaultConfiguration(),
            moduleIncludes = includes
        )
        return applicationMetadata
    }

    private fun getConfigurationScans(annotations: Sequence<KSAnnotation>): Set<KoinMetaData.ConfigurationTag>? {
        val annotation = annotations.firstOrNull { it.shortName.asString() == KoinApplication::class.simpleName!! }
        if (annotation == null) return defaultConfiguration()
        return configurationValue(annotation,"configurations")
    }

    private fun getIncludes(annotations: Sequence<KSAnnotation>): List<KoinMetaData.ModuleInclude>? {
        val annotation = annotations.firstOrNull { it.shortName.asString() == KoinApplication::class.simpleName!! }
        return annotation?.let {
            includedModules(annotation).toModuleIncludes()
        }
    }
}

