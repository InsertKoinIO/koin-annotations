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
import org.koin.compiler.scanner.ext.filterForbiddenKeywords
import org.koin.compiler.scanner.ext.getKoinAnnotations
import org.koin.compiler.scanner.ext.getQualifier
import org.koin.compiler.scanner.ext.getScopeAnnotation

class FunctionComponentScanner(
    val logger: KSPLogger
) : FunctionScanner(isModuleFunction = false) {

    fun createFunctionDefinition(element: KSAnnotated): KoinMetaData.Definition? {
        return addFunctionDefinition(element)
    }

    private fun addFunctionDefinition(element: KSAnnotated): KoinMetaData.Definition? {
        val ksFunctionDeclaration = (element as KSFunctionDeclaration)
        val packageName = ksFunctionDeclaration.packageName.asString().filterForbiddenKeywords()
        val returnedType = ksFunctionDeclaration.returnType?.resolve()?.declaration?.simpleName?.toString()
        val qualifier = ksFunctionDeclaration.getQualifier()

        return returnedType?.let {
            val functionName = ksFunctionDeclaration.simpleName.asString()
            if (ksFunctionDeclaration.parent is KSClassDeclaration){
                logger.logging("parent is KClass ${ksFunctionDeclaration.parent} skipping as it should be in an inner class")
                return null
            }

            val annotations = element.getKoinAnnotations()
            val scopeAnnotation = annotations.getScopeAnnotation()

            val definition = if (scopeAnnotation != null){
                declareDefinition(scopeAnnotation.first, scopeAnnotation.second, packageName, qualifier, functionName, ksFunctionDeclaration, annotations)
            } else {
                annotations.firstNotNullOf { (annotationName, annotation) ->
                    declareDefinition(annotationName, annotation, packageName, qualifier, functionName, ksFunctionDeclaration, annotations)
                }
            }
            definition
        }
    }
}