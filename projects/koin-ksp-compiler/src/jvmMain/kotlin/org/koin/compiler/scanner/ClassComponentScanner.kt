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
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.metadata.*
import org.koin.compiler.scanner.ext.*

class ClassComponentScanner(
    val logger: KSPLogger,
) {

    fun createClassDefinition(element: KSAnnotated): KoinMetaData.Definition {
        val ksClassDeclaration = (element as KSClassDeclaration)
        val packageName = ksClassDeclaration.getPackageName().filterForbiddenKeywords()
        val qualifiedName = ksClassDeclaration.qualifiedName?.asString() ?: ""
        val qualifier = ksClassDeclaration.getQualifier()
        val annotations = element.getKoinAnnotations()
        val scopeAnnotation = annotations.getScopeAnnotation()
        return if (scopeAnnotation != null){
            createClassDefinition(element, scopeAnnotation.second, ksClassDeclaration, scopeAnnotation.first, packageName, qualifier, qualifiedName, annotations)
        } else {
            annotations.firstNotNullOf { (annotationName, annotation) ->
                createClassDefinition(element, annotation, ksClassDeclaration, annotationName, packageName, qualifier, qualifiedName, annotations)
            }
        }
    }

    private fun createClassDefinition(
        element: KSAnnotated,
        annotation: KSAnnotation,
        ksClassDeclaration: KSClassDeclaration,
        annotationName: String,
        packageName: String,
        qualifier: String?,
        qualifiedName: String,
        annotations: Map<String, KSAnnotation> = emptyMap()
    ): KoinMetaData.Definition.ClassDefinition {
        val declaredBindings = declaredBindings(annotation)
        val defaultBindings = ksClassDeclaration.superTypes.map { it.resolve().declaration }.toList()
        val forceDeclaredBindings = declaredBindings?.hasDefaultUnitValue() == false && declaredBindings.isNotEmpty()
        val allBindings: List<KSDeclaration> = if (forceDeclaredBindings) declaredBindings else defaultBindings
        val ctorParams = ksClassDeclaration.primaryConstructor?.parameters?.getParameters()

        val isExpect = ksClassDeclaration.isExpect
        val isActual = ksClassDeclaration.isActual

        return when (annotationName) {
            SINGLE.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, qualifiedName, ctorParams, allBindings, isExpect, isActual = isActual)
            }

            SINGLETON.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, qualifiedName, ctorParams, allBindings, isExpect, isActual = isActual)
            }

            FACTORY.annotationName -> {
                createClassDefinition(FACTORY,packageName, qualifier, qualifiedName, ctorParams, allBindings, isExpect = isExpect, isActual = isActual)
            }

            KOIN_VIEWMODEL.annotationName -> {
                createClassDefinition(KOIN_VIEWMODEL,packageName, qualifier, qualifiedName, ctorParams, allBindings, isExpect = isExpect, isActual = isActual)
            }
            KOIN_WORKER.annotationName -> {
                createClassDefinition(KOIN_WORKER,packageName, qualifier, qualifiedName, ctorParams, allBindings, isExpect = isExpect, isActual = isActual)
            }
            SCOPE.annotationName -> {
                val (scopeData, extraScopeBindings, extraAnnotationDefinition) = getAnnotationScopeData(annotation, annotations, allBindings)
                createClassDefinition(extraAnnotationDefinition ?: SCOPE,packageName, qualifier, qualifiedName, ctorParams, extraScopeBindings,scope = scopeData, isExpect = isExpect, isActual = isActual)
            }
            else -> error("Unknown annotation type: $annotationName")
        }
    }

    private fun createSingleDefinition(
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        qualifiedName: String,
        ctorParams: List<KoinMetaData.DefinitionParameter>?,
        allBindings: List<KSDeclaration>,
        isExpect : Boolean,
        isActual : Boolean
    ): KoinMetaData.Definition.ClassDefinition {
        val createdAtStart: Boolean =
            annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean? ?: false
        return createClassDefinition(SINGLE, packageName, qualifier, qualifiedName, ctorParams, allBindings, isCreatedAtStart = createdAtStart, isExpect= isExpect, isActual = isActual)
    }

    private fun createClassDefinition(
        keyword : DefinitionAnnotation,
        packageName: String,
        qualifier: String?,
        qualifiedName: String,
        ctorParams: List<KoinMetaData.DefinitionParameter>?,
        allBindings: List<KSDeclaration>,
        isCreatedAtStart : Boolean? = null,
        scope: KoinMetaData.Scope? = null,
        isExpect : Boolean,
        isActual : Boolean
    ): KoinMetaData.Definition.ClassDefinition {
        return KoinMetaData.Definition.ClassDefinition(
            packageName = packageName,
            qualifier = qualifier,
            isCreatedAtStart = isCreatedAtStart,
            qualifiedName = qualifiedName,
            constructorParameters = ctorParams ?: emptyList(),
            bindings = allBindings,
            keyword = keyword,
            scope = scope,
            isExpect = isExpect,
            isActual = isActual
        )
    }
}