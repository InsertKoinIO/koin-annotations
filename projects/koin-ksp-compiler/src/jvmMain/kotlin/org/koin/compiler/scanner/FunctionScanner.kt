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
import org.koin.compiler.scanner.ext.getAnnotationScopeData

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
        val isActual = ksFunctionDeclaration.isActual

        val foundAnnotation = DEFINITION_ANNOTATION_MAP[annotationName]
        return if (foundAnnotation == null) null
        else {
            // single case
            val createdAtStart: Boolean? = annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as Boolean?
            // scope case
            val scopeValues = if (foundAnnotation == SCOPE) getAnnotationScopeData(annotation, annotations, allBindings) else null
            val scopeData = scopeValues?.scopeData
            val extraAnnotationDefinition = scopeValues?.extraAnnotationDefinition

            KoinMetaData.Definition.FunctionDefinition(
                packageName = packageName,
                qualifier = qualifier,
                isCreatedAtStart = createdAtStart,
                functionName = functionName,
                parameters = functionParameters,
                bindings = allBindings,
                keyword = extraAnnotationDefinition ?: foundAnnotation,
                scope = scopeData,
                isExpect = isExpect,
                isActual = isActual
            ).apply { isClassFunction = isModuleFunction }
        }
    }

}