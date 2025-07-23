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
        val parent = ksClassDeclaration.parentDeclaration?.simpleName?.asString()
        val packageName = (ksClassDeclaration.getPackageName() + (parent?.let { ".$it" } ?: "")).filterForbiddenKeywords()
        val className = ksClassDeclaration.simpleName.asString()
        val qualifier = ksClassDeclaration.getQualifier()
        val annotations = element.getKoinAnnotations()
        val scopeAnnotation = annotations.getScopeAnnotation()

        return if (scopeAnnotation != null){
            createClassDefinition(element, scopeAnnotation.second, ksClassDeclaration, scopeAnnotation.first, packageName, qualifier, className, annotations)
        } else {
            annotations.firstNotNullOf { (annotationName, annotation) ->
                createClassDefinition(element, annotation, ksClassDeclaration, annotationName, packageName, qualifier, className, annotations)
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
        className: String,
        annotations: Map<String, KSAnnotation> = emptyMap()
    ): KoinMetaData.Definition.ClassDefinition {
        val declaredBindings = declaredBindings(annotation)
        val defaultBindings = ksClassDeclaration.superTypes.map { it.resolve().declaration }.toList()
        val forceDeclaredBindings = declaredBindings?.hasDefaultUnitValue() == false && declaredBindings.isNotEmpty()
        val allBindings: List<KSDeclaration> = if (forceDeclaredBindings) declaredBindings else defaultBindings

        val ctorParams = ksClassDeclaration.primaryConstructor?.parameters?.getParameters()

        val isExpect = ksClassDeclaration.isExpect
        val isActual = ksClassDeclaration.isActual

        val foundAnnotation = DEFINITION_ANNOTATION_MAP[annotationName]
        return if (foundAnnotation == null){
            error("Unknown annotation type: $annotationName")
        } else {
            // single case
            val createdAtStart: Boolean? = annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean?
            // scope case
            val scopeValues = if (foundAnnotation == SCOPE) getAnnotationScopeData(annotation, annotations, allBindings) else null
            val scopeData = scopeValues?.scopeData
            val extraAnnotationDefinition = scopeValues?.extraAnnotationDefinition

            KoinMetaData.Definition.ClassDefinition(
                packageName = packageName,
                qualifier = qualifier,
                isCreatedAtStart = createdAtStart,
                className = className,
                constructorParameters = ctorParams ?: emptyList(),
                bindings = allBindings,
                keyword = extraAnnotationDefinition ?: foundAnnotation,
                scope = scopeData,
                isExpect = isExpect,
                isActual = isActual
            )
        }
    }
}