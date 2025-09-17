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
package org.koin.compiler.metadata

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import org.koin.android.annotation.KoinViewModel
import org.koin.android.annotation.KoinWorker
import org.koin.compiler.metadata.KoinMetaData.ConfigurationTag.Companion.DEFAULT
import org.koin.core.annotation.*
import java.util.*
import kotlin.reflect.KClass

interface DefinitionAnnotation {
    val keyword: String
    val import: String?
    val parentKeyword: DefinitionAnnotation?
    val annotationSimpleName : String
    val annotationQualifiedName : String
}

data class DefinitionClassAnnotation(
    override val keyword: String,
    override val import: String? = null,
    val annotationType: KClass<*>,
    override val parentKeyword: DefinitionAnnotation? = null,
) :DefinitionAnnotation {
    override val annotationSimpleName: String = annotationType.simpleName!!
    override val annotationQualifiedName: String = annotationType.qualifiedName!!
}

data class DefinitionNamedAnnotation(
    override val keyword: String,
    override val import: String? = null,
    override val annotationSimpleName: String,
    override val annotationQualifiedName: String,
    override val parentKeyword: DefinitionAnnotation? = null,
) :DefinitionAnnotation

val SINGLE = DefinitionClassAnnotation("single", annotationType = Single::class)
val SINGLETON = DefinitionNamedAnnotation("single", null, "Singleton","jakarta.inject.Singleton")
val JAVAX_SINGLETON = DefinitionNamedAnnotation("single", null, "Singleton","javax.inject.Singleton")
val FACTORY = DefinitionClassAnnotation("factory", annotationType = Factory::class)
val INJECT = DefinitionNamedAnnotation("factory", null, "Inject","jakarta.inject.Inject")
val SCOPE = DefinitionClassAnnotation("scoped", annotationType = Scope::class)
val SCOPED = DefinitionClassAnnotation("scoped", annotationType = Scoped::class)

@Deprecated("To be use with KOIN_VIEWMODEL")
val KOIN_VIEWMODEL_ANDROID = DefinitionClassAnnotation("viewModel", "org.koin.androidx.viewmodel.dsl.viewModel", KoinViewModel::class)
val KOIN_VIEWMODEL = DefinitionClassAnnotation("viewModel", "org.koin.core.module.dsl.viewModel", KoinViewModel::class)

val KOIN_WORKER = DefinitionClassAnnotation("worker", "org.koin.androidx.workmanager.dsl.worker", KoinWorker::class)

val DEFINITION_ANNOTATION_LIST = listOf(SINGLE, SINGLETON, JAVAX_SINGLETON, FACTORY, INJECT, SCOPE, SCOPED,KOIN_VIEWMODEL, KOIN_WORKER) + SCOPE_ARCHETYPES_LIST

val DEFINITION_ANNOTATION_LIST_TYPES = DEFINITION_ANNOTATION_LIST.map { it.annotationQualifiedName }
val DEFINITION_ANNOTATION_LIST_NAMES = DEFINITION_ANNOTATION_LIST.map { it.annotationSimpleName.lowercase(Locale.getDefault()) }
val DEFINITION_ANNOTATION_MAP = DEFINITION_ANNOTATION_LIST.associate { it.annotationSimpleName to it }

val SCOPE_DEFINITION_ANNOTATION_LIST = listOf(SCOPED, FACTORY,INJECT, KOIN_VIEWMODEL, KOIN_WORKER) + SCOPE_ARCHETYPES_LIST
val SCOPE_DEFINITION_ANNOTATION_LIST_NAMES = SCOPE_DEFINITION_ANNOTATION_LIST.map { it.annotationSimpleName.lowercase(Locale.getDefault()) }


fun isValidAnnotation(s: String): Boolean = s.lowercase(Locale.getDefault()) in DEFINITION_ANNOTATION_LIST_NAMES
fun isValidScopeExtraAnnotation(s: String): Boolean = s.lowercase(Locale.getDefault()) in SCOPE_DEFINITION_ANNOTATION_LIST_NAMES
fun isScopeAnnotation(s: String): Boolean = s.equals(SCOPE.annotationSimpleName, ignoreCase = true)

fun getExtraScopeAnnotation(annotations: Map<String, KSAnnotation>): DefinitionAnnotation? {
    val key = annotations.keys.firstOrNull { k -> isValidScopeExtraAnnotation(k) }
    val definitionAnnotation = when (key) {
        SCOPED.annotationSimpleName -> SCOPED
        FACTORY.annotationSimpleName -> FACTORY
        KOIN_VIEWMODEL.annotationSimpleName -> KOIN_VIEWMODEL
        KOIN_WORKER.annotationSimpleName -> KOIN_WORKER
        else -> null
    }
    return definitionAnnotation
}

fun declaredBindings(annotation: KSAnnotation): List<KSDeclaration>? {
    val declaredBindingsTypes = annotation.arguments.firstOrNull { it.name?.asString() == "binds" }?.value as? List<KSType>?
    return declaredBindingsTypes?.map { it.declaration }
}

fun List<KSDeclaration>.hasDefaultUnitValue() : Boolean {
    return size == 1 && first().simpleName.asString() == "Unit"
}

fun includedModules(annotation: KSAnnotation, fieldName : String = "includes"): List<KSDeclaration>? {
    val declaredBindingsTypes = annotation.arguments.firstOrNull { it.name?.asString() == fieldName }?.value as? List<KSType>?
    return declaredBindingsTypes?.map { it.declaration }
}

fun componentsScanValue(annotation: KSAnnotation): Set<KoinMetaData.Module.ComponentScan> {
    val values = extractValueStringList(annotation) ?: return setOf(KoinMetaData.Module.ComponentScan(""))
    return values.map { KoinMetaData.Module.ComponentScan(it.trim()) }.toSet()
}

fun configurationValue(annotation: KSAnnotation, field : String =  "value"): Set<KoinMetaData.ConfigurationTag> {
    val values = extractValueStringList(annotation, DEFAULT.name, field)
    return if (values == null || values.isEmpty() || values == arrayListOf("")) {
        defaultConfiguration()
    } else {
        values.map { KoinMetaData.ConfigurationTag(it.trim()) }.toSet()
    }
}

@Suppress("UNCHECKED_CAST")
private fun extractValueStringList(annotation: KSAnnotation, defaultValue : String = "", fieldName : String = "value"): List<String>? {
    val csValue = annotation.arguments.firstOrNull { arg -> arg.name?.asString() == fieldName }?.value
    val csValueList = csValue as? List<String>
    val values = if (csValueList?.isEmpty() == true) listOf(defaultValue) else csValueList
    return values
}

fun isCreatedAtStart(annotation: KSAnnotation): Boolean? {
    return annotation.arguments.firstOrNull { it.name?.asString() == "createdAtStart" }?.value as? Boolean
}
