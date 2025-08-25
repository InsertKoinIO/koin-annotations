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
        val applicationAnnotation = declaration.annotations.firstOrNull { it.shortName.asString() == KoinApplication::class.simpleName!! }
        val configurations = getConfigurationScans(applicationAnnotation)
        val includes = getIncludes(applicationAnnotation)

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
            configurationTags = configurations,
            moduleIncludes = includes
        )
        return applicationMetadata
    }

    private fun getConfigurationScans(annotation: KSAnnotation?): Set<KoinMetaData.ConfigurationTag> {
        if (annotation == null) return defaultConfiguration()
        return configurationValue(annotation,"configurations")
    }

    private fun getIncludes(annotation: KSAnnotation?): List<KoinMetaData.ModuleInclude>? {
        return annotation?.let {
            includedModules(annotation, "modules")
                // filter default value
                ?.filter { it.qualifiedName?.asString() != "kotlin.Unit" }
                .toModuleIncludes()
        }
    }
}

