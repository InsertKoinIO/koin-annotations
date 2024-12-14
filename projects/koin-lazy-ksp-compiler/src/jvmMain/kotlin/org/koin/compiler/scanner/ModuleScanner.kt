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

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import org.koin.compiler.metadata.*
import org.koin.compiler.scanner.ext.*

class ModuleScanner(
    val logger: KSPLogger
) : FunctionScanner(isModuleFunction = true) {

    fun createClassModule(element: KSAnnotated): KoinMetaData.Module {
        val declaration = (element as KSClassDeclaration)
        val modulePackage = declaration.getPackageName().filterForbiddenKeywords()
        val annotations = declaration.annotations
        val includes = getIncludedModules(annotations)
        val isCreatedAtStart = getIsCreatedAtStart(annotations)
        val componentScan = getComponentScan(annotations)
        val isExpect = declaration.isExpect

        val name = "$element"
        val type = if (declaration.classKind == ClassKind.OBJECT) {
            KoinMetaData.ModuleType.OBJECT
        } else  {
            KoinMetaData.ModuleType.CLASS
        }

        val moduleMetadata = KoinMetaData.Module(
            packageName = modulePackage,
            name = name,
            type = type,
            componentScan = componentScan,
            includes = includes.toModuleIncludes(),
            isCreatedAtStart = isCreatedAtStart,
            visibility = declaration.getVisibility(),
            isExpect = isExpect
        )

        val annotatedFunctions = declaration.getAllFunctions()
            .filter {
                it.annotations.map { a -> a.shortName.asString() }.any { a -> isValidAnnotation(a) }
            }
            .toList()

        val definitions = annotatedFunctions.mapNotNull { addFunctionDefinition(it) }
        moduleMetadata.definitions += definitions

        return moduleMetadata
    }

    private fun getIncludedModules(annotations: Sequence<KSAnnotation>) : List<KSDeclaration>? {
        val module = annotations.firstOrNull { it.shortName.asString() == "Module" }
        return module?.let { includedModules(it) }
    }

    private fun getIsCreatedAtStart(annotations: Sequence<KSAnnotation>) : Boolean? {
        val module = annotations.firstOrNull { it.shortName.asString() == "Module" }
        return module?.let { isCreatedAtStart(it) }
    }

    private fun getComponentScan(annotations: Sequence<KSAnnotation>): KoinMetaData.Module.ComponentScan? {
        val componentScan = annotations.firstOrNull { it.shortName.asString() == "ComponentScan" }
        return componentScan?.let { a ->
            val value : String = a.arguments.firstOrNull { arg -> arg.name?.asString() == "value" }?.value as? String? ?: ""
            KoinMetaData.Module.ComponentScan(value)
        }
    }

    private fun addFunctionDefinition(element: KSAnnotated): KoinMetaData.Definition? {
        val ksFunctionDeclaration = (element as KSFunctionDeclaration)
        val packageName = ksFunctionDeclaration.packageName.asString().filterForbiddenKeywords()
        val returnedType = ksFunctionDeclaration.returnType?.resolve()?.declaration?.simpleName?.toString()
        val qualifier = ksFunctionDeclaration.getQualifier()

        return returnedType?.let {
            val functionName = ksFunctionDeclaration.simpleName.asString()
            val annotations = element.getKoinAnnotations()
            val scopeAnnotation = annotations.getScopeAnnotation()

            return if (scopeAnnotation != null){
                declareDefinition(scopeAnnotation.first, scopeAnnotation.second, packageName, qualifier, functionName, ksFunctionDeclaration, annotations)
            } else {
                annotations.firstNotNullOf { (annotationName, annotation) ->
                    declareDefinition(annotationName, annotation, packageName, qualifier, functionName, ksFunctionDeclaration, annotations)
                }
            }
        }
    }

    private fun List<KSDeclaration>?.toModuleIncludes(): List<KoinMetaData.ModuleInclude>? {
        return this?.map {
            it.mapModuleInclude()
        }
    }

    private fun KSDeclaration.mapModuleInclude(): KoinMetaData.ModuleInclude {
        val packageName: String = packageName.asString()
        val className = simpleName.asString()
        return KoinMetaData.ModuleInclude(packageName, className, isExpect)
    }
}

