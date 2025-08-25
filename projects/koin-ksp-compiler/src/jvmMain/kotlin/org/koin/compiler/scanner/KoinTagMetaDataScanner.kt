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
import org.koin.meta.annotations.MetaApplication
import org.koin.meta.annotations.MetaDefinition
import org.koin.meta.annotations.MetaModule

class KoinTagMetaDataScanner(
    private val logger: KSPLogger,
) {
    internal lateinit var resolver: Resolver

    fun findInvalidSymbols(): List<KSAnnotated> {
        val invalidSymbols = resolver.getSymbols<MetaModule>(isValid = false) + resolver.getSymbols<MetaModule>(isValid = false) + resolver.getSymbols<MetaApplication>(isValid = false)
        if (invalidSymbols.isNotEmpty()) {
            logger.logging("Invalid definition symbols found.")
            logInvalidEntities(invalidSymbols)
            return invalidSymbols
        }

        return emptyList()
    }

    private fun logInvalidEntities(classDeclarationList: List<KSAnnotated>) {
        classDeclarationList.forEach { logger.logging("Invalid entity: $it") }
    }

    fun findMetaModules(): List<KSAnnotation> {
        return resolver.getAnnotationForSymbols<MetaModule>(isValid = true)
    }

    fun findMetaDefinitions(): List<KSAnnotation> {
        return resolver.getAnnotationForSymbols<MetaDefinition>(isValid = true)
    }

    fun findMetaApplications(): List<KSAnnotation> {
        return resolver.getAnnotationForSymbols<MetaApplication>(isValid = true)
    }

    inline fun <reified T> Resolver.getSymbols(isValid : Boolean): List<KSAnnotated> {
        return this.getSymbolsWithAnnotation(T::class.qualifiedName!!)
            .filter { isValid == it.validate() }
            .toList()
    }

    inline fun <reified T> Resolver.getAnnotationForSymbols(isValid : Boolean): List<KSAnnotation> {
        return this.getSymbols<T>(isValid).map { it.annotations.first() }
    }
}
