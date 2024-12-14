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
import org.koin.compiler.scanner.ext.*

class ClassComponentScanner(
    val logger: KSPLogger,
) {

    fun createClassDefinition(element: KSAnnotated): KoinMetaData.Definition {
        val ksClassDeclaration = (element as KSClassDeclaration)
        val packageName = ksClassDeclaration.getPackageName().filterForbiddenKeywords()
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
        val allBindings: List<KSDeclaration> = if (declaredBindings?.hasDefaultUnitValue() == false) declaredBindings else defaultBindings
        val ctorParams = ksClassDeclaration.primaryConstructor?.parameters?.getParameters()

        val isExpect = ksClassDeclaration.isExpect
//        val isActual = ksClassDeclaration.isActual
//        LOGGER.info("definition - $packageName $className - isExpect:$isExpect isActual:$isActual")

        return when (annotationName) {
            SINGLE.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, className, ctorParams, allBindings, isExpect)
            }
            SINGLETON.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, className, ctorParams, allBindings, isExpect)
            }
            LAZY_SINGLE.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, className, ctorParams, allBindings, isExpect, isLazy = true)
            }
            FACTORY.annotationName -> {
                createClassDefinition(FACTORY,packageName, qualifier, className, ctorParams, allBindings, isExpect = isExpect)
            }
            LAZY_FACTORY.annotationName -> {
                createClassDefinition(LAZY_FACTORY,packageName, qualifier, className, ctorParams, allBindings, isExpect = isExpect, isLazy = true)
            }
            KOIN_VIEWMODEL.annotationName -> {
                createClassDefinition(KOIN_VIEWMODEL,packageName, qualifier, className, ctorParams, allBindings, isExpect = isExpect)
            }
            KOIN_WORKER.annotationName -> {
                createClassDefinition(KOIN_WORKER,packageName, qualifier, className, ctorParams, allBindings, isExpect = isExpect)
            }
            SCOPE.annotationName -> {
                val scopeData : KoinMetaData.Scope = annotation.arguments.getScope()
                val extraAnnotationDefinition = getExtraScopeAnnotation(annotations)
                val extraAnnotation = annotations[extraAnnotationDefinition?.annotationName]
                val extraDeclaredBindings = extraAnnotation?.let { declaredBindings(it) }
                val extraScopeBindings = if(extraDeclaredBindings?.hasDefaultUnitValue() == false) extraDeclaredBindings else allBindings
                createClassDefinition(extraAnnotationDefinition ?: SCOPE,packageName, qualifier, className, ctorParams, extraScopeBindings,scope = scopeData, isExpect = isExpect)
            }
            else -> error("Unknown annotation type: $annotationName")
        }
    }

    private fun createSingleDefinition(
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        className: String,
        ctorParams: List<KoinMetaData.DefinitionParameter>?,
        allBindings: List<KSDeclaration>,
        isExpect : Boolean,
        isLazy: Boolean = false
    ): KoinMetaData.Definition.ClassDefinition {
        val createdAtStart: Boolean =
            annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean? ?: false
        return createClassDefinition(SINGLE, packageName, qualifier, className, ctorParams, allBindings, isCreatedAtStart = createdAtStart, isExpect= isExpect)
    }

    private fun createClassDefinition(
        keyword : DefinitionAnnotation,
        packageName: String,
        qualifier: String?,
        className: String,
        ctorParams: List<KoinMetaData.DefinitionParameter>?,
        allBindings: List<KSDeclaration>,
        isCreatedAtStart : Boolean? = null,
        scope: KoinMetaData.Scope? = null,
        isExpect : Boolean,
        isLazy: Boolean = false
    ): KoinMetaData.Definition.ClassDefinition {
        return KoinMetaData.Definition.ClassDefinition(
            packageName = packageName,
            qualifier = qualifier,
            isCreatedAtStart = isCreatedAtStart,
            className = className,
            constructorParameters = ctorParams ?: emptyList(),
            bindings = allBindings,
            keyword = keyword,
            scope = scope,
            isExpect = isExpect,
            isLazy = isLazy
        )
    }
}