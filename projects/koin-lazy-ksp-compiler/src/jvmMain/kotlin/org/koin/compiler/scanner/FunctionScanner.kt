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

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import org.koin.compiler.metadata.*
import org.koin.compiler.scanner.ext.getParameters
import org.koin.compiler.scanner.ext.getScope

/**
 * Scan for Koin function component metadata
 *
 * @author Arnaud Giuliani
 */
abstract class FunctionScanner(
    private val isModuleFunction : Boolean
) {

    fun declareDefinition(
        annotationName: String,
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        ksFunctionDeclaration: KSFunctionDeclaration,
        annotations: Map<String, KSAnnotation> = emptyMap()
    ): KoinMetaData.Definition.FunctionDefinition? {
        val foundBindings: List<KSDeclaration> = declaredBindings(annotation)?.let { if (!it.hasDefaultUnitValue()) it else emptyList() } ?: emptyList()
        val returnedType: KSDeclaration? = ksFunctionDeclaration.returnType?.resolve()?.declaration
        val allBindings: List<KSDeclaration> =  returnedType?.let { foundBindings + it } ?: foundBindings
        val functionParameters = ksFunctionDeclaration.parameters.getParameters()
        val isExpect = ksFunctionDeclaration.isExpect

        return when (annotationName) {
            SINGLE.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, functionName, functionParameters, allBindings, isExpect)
            }
            SINGLETON.annotationName -> {
                createSingleDefinition(annotation, packageName, qualifier, functionName, functionParameters, allBindings, isExpect)
            }
            FACTORY.annotationName -> {
                createDefinition(FACTORY,packageName,qualifier,functionName,functionParameters,allBindings, isExpect = isExpect)
            }
            KOIN_VIEWMODEL.annotationName -> {
                createDefinition(KOIN_VIEWMODEL,packageName,qualifier,functionName,functionParameters,allBindings, isExpect = isExpect)
            }
            KOIN_WORKER.annotationName -> {
                createDefinition(KOIN_WORKER,packageName,qualifier,functionName,functionParameters,allBindings, isExpect = isExpect)
            }
            SCOPE.annotationName -> {
                val scopeData : KoinMetaData.Scope = annotation.arguments.getScope()
                val extraAnnotation = getExtraScopeAnnotation(annotations)
                createDefinition(extraAnnotation ?: SCOPE,packageName,qualifier,functionName,functionParameters,allBindings,scope = scopeData, isExpect = isExpect)
            }
            else -> null
        }
    }

    private fun createSingleDefinition(
        annotation: KSAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        functionParameters: List<KoinMetaData.DefinitionParameter>,
        allBindings: List<KSDeclaration>,
        isExpect : Boolean
    ): KoinMetaData.Definition.FunctionDefinition {
        val createdAtStart: Boolean =
            annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean?
                ?: false
        return createDefinition(
            SINGLE,
            packageName,
            qualifier,
            functionName,
            functionParameters,
            allBindings,
            isCreatedAtStart = createdAtStart,
            isExpect = isExpect
        )
    }

    private fun createDefinition(
        keyword : DefinitionAnnotation,
        packageName: String,
        qualifier: String?,
        functionName: String,
        parameters: List<KoinMetaData.DefinitionParameter>?,
        allBindings: List<KSDeclaration>,
        isCreatedAtStart : Boolean? = null,
        scope: KoinMetaData.Scope? = null,
        isExpect : Boolean
    ): KoinMetaData.Definition.FunctionDefinition {
        return KoinMetaData.Definition.FunctionDefinition(
            packageName = packageName,
            qualifier = qualifier,
            isCreatedAtStart = isCreatedAtStart,
            functionName = functionName,
            parameters = parameters ?: emptyList(),
            bindings = allBindings,
            keyword = keyword,
            scope = scope,
            isExpect = isExpect
        ).apply { isClassFunction = isModuleFunction }
    }

}