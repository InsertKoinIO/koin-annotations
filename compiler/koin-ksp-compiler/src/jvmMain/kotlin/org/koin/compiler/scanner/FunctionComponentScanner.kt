/*
 * Copyright 2017-2022 the original author or authors.
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

class FunctionComponentScanner(
    val logger: KSPLogger
) {

    fun createFunctionDefinition(element: KSAnnotated): KoinMetaData.Definition {
        return addFunctionDefinition(element)
    }

    private fun addFunctionDefinition(element: KSAnnotated): KoinMetaData.Definition {
        val ksFunctionDeclaration = (element as KSFunctionDeclaration)
        val packageName = ksFunctionDeclaration.containingFile!!.packageName.asString().filterForbiddenKeywords()
        val returnedType = ksFunctionDeclaration.returnType?.resolve()?.declaration?.simpleName?.toString()
        val qualifier = ksFunctionDeclaration.getStringQualifier()

        return returnedType?.let {
            val functionName = ksFunctionDeclaration.simpleName.asString()

            val annotations = element.getKoinAnnotations()
            val scopeAnnotation = annotations.getScopeAnnotation()

            val definition = if (scopeAnnotation != null){
                declareDefinition(scopeAnnotation.first, scopeAnnotation.second, packageName, qualifier, functionName, ksFunctionDeclaration, annotations)
            } else {
                annotations.firstNotNullOf { (annotationName, annotation) ->
                    declareDefinition(annotationName, annotation, packageName, qualifier, functionName, ksFunctionDeclaration, annotations)
                }
            }
            definition ?: error("Couldn't create function definition for $ksFunctionDeclaration from $packageName")
        } ?: error("Can't resolve function definition $ksFunctionDeclaration from $packageName as returned type can't be resolved")
    }

    //TODO Refactor/Extract here?

    private fun declareDefinition(
        annotationName: String,
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        ksFunctionDeclaration: KSFunctionDeclaration,
        annotations: Map<String, KSAnnotation> = emptyMap()
    ): KoinMetaData.Definition.FunctionDefinition? {
        val allBindings = declaredBindings(annotation) ?: emptyList()
        val functionParameters = ksFunctionDeclaration.parameters.getConstructorParameters()

        return when (annotationName) {
            SINGLE.annotationName -> {
                createFunctionDefinition(annotation, packageName, qualifier, functionName, functionParameters, allBindings)
            }
            SINGLETON.annotationName -> {
                createFunctionDefinition(annotation, packageName, qualifier, functionName, functionParameters, allBindings)
            }
            FACTORY.annotationName -> {
                createFunctionDefinition(FACTORY,packageName,qualifier,functionName,functionParameters,allBindings)
            }
            KOIN_VIEWMODEL.annotationName -> {
                createFunctionDefinition(KOIN_VIEWMODEL,packageName,qualifier,functionName,functionParameters,allBindings)
            }
            KOIN_WORKER.annotationName -> {
                createFunctionDefinition(KOIN_WORKER,packageName,qualifier,functionName,functionParameters,allBindings)
            }
            SCOPE.annotationName -> {
                val scopeData : KoinMetaData.Scope = annotation.arguments.getScope()
                val extraAnnotation = getExtraScopeAnnotation(annotations)
                createFunctionDefinition(extraAnnotation ?: SCOPE,packageName,qualifier,functionName,functionParameters,allBindings,scope = scopeData)
            }
            else -> null
        }
    }

    private fun createFunctionDefinition(
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        functionParameters: List<KoinMetaData.ConstructorParameter>,
        allBindings: List<KSDeclaration>
    ): KoinMetaData.Definition.FunctionDefinition {
        val createdAtStart: Boolean =
            annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean?
                ?: false
        return createFunctionDefinition(
            SINGLE,
            packageName,
            qualifier,
            functionName,
            functionParameters,
            allBindings,
            isCreatedAtStart = createdAtStart
        )
    }

    private fun createFunctionDefinition(
        keyword : DefinitionAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        parameters: List<KoinMetaData.ConstructorParameter>?,
        allBindings: List<KSDeclaration>,
        isCreatedAtStart : Boolean? = null,
        scope: KoinMetaData.Scope? = null,
    ) = KoinMetaData.Definition.FunctionDefinition(
        packageName = packageName,
        qualifier = qualifier,
        isCreatedAtStart = isCreatedAtStart,
        functionName = functionName,
        parameters = parameters ?: emptyList(),
        bindings = allBindings,
        keyword = keyword,
        scope = scope,
        isClassFunction = false
    )
}