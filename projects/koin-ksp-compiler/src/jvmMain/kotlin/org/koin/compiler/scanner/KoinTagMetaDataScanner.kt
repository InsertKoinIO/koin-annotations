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
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.validate
import org.koin.meta.annotations.MetaDefinition
import org.koin.meta.annotations.MetaModule

class KoinTagMetaDataScanner(
    private val logger: KSPLogger,
    private val resolver: Resolver
) {

    fun findInvalidSymbols(): List<KSAnnotated> {
        val invalidModuleSymbols = resolver.getMetaModuleSymbols(isValid = false)
        val invalidDefinitionSymbols = resolver.getMetaDefinitionSymbols(isValid = false)

        val invalidSymbols = invalidModuleSymbols + invalidDefinitionSymbols
        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid definition symbols found.")
            logInvalidEntities(invalidSymbols)
            return invalidSymbols
        }

        return emptyList()
    }

    fun findMetaModules(): List<KSAnnotation> {
        logger.logging("scan meta modules ...")
        return resolver.getMetaModuleSymbols(isValid = true).map { it.annotations.first() }
    }

    fun findMetaDefinitions(): List<KSAnnotation> {
        logger.logging("scan meta definitions ...")
        return resolver.getMetaDefinitionSymbols(isValid = true).map { it.annotations.first() }
    }

    private fun Resolver.getMetaModuleSymbols(isValid : Boolean): List<KSAnnotated> {
        return this.getSymbolsWithAnnotation(MetaModule::class.qualifiedName!!)
            .filter { isValid == it.validate() }
            .toList()
    }

    private fun Resolver.getMetaDefinitionSymbols(isValid : Boolean): List<KSAnnotated> {
        return this.getSymbolsWithAnnotation(MetaDefinition::class.qualifiedName!!)
            .filter { isValid == it.validate() }
            .toList()
    }

    private fun logInvalidEntities(classDeclarationList: List<KSAnnotated>) {
        classDeclarationList.forEach { logger.logging("Invalid entity: $it") }
    }

}
