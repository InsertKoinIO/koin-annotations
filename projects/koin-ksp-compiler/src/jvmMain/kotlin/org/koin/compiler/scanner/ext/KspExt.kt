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
package org.koin.compiler.scanner.ext

import com.google.devtools.ksp.symbol.*
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.isScopeAnnotation
import org.koin.compiler.metadata.isValidAnnotation
import org.koin.compiler.type.forbiddenKeywords
import org.koin.core.annotation.*

fun KSAnnotated.getKoinAnnotations(): Map<String, KSAnnotation> {
    return annotations
        .filter { isValidAnnotation(it.shortName.asString()) }
        .map { annotation -> Pair(annotation.shortName.asString(), annotation) }
        .toMap()
}

fun Map<String, KSAnnotation>.getScopeAnnotation(): Pair<String, KSAnnotation>? {
    return firstNotNullOfOrNull { (name, annotation) ->
        if (isScopeAnnotation(name)) Pair(name, annotation) else null
    }
}

fun List<KSValueArgument>.getScope(): KoinMetaData.Scope {
    val scopeKClassType: KSType? = firstOrNull { it.name?.asString() == "value" }?.value as? KSType
    val scopeStringType: String? = firstOrNull { it.name?.asString() == "name" }?.value as? String
    return scopeKClassType?.let {
        val type = it.declaration
        if (type.simpleName.asString() != "Unit") {
            KoinMetaData.Scope.ClassScope(type)
        } else null
    }
        ?: scopeStringType?.let { KoinMetaData.Scope.StringScope(it) }
        ?: error("Scope annotation needs parameters: either type value or name")
}

fun List<KSValueArgument>.getNamed(): KoinMetaData.Named {
    val namedKClassType: KSType? = firstOrNull { it.name?.asString() == "type" }?.value as? KSType
    val namedStringType: String? = firstOrNull { it.name?.asString() == "value" }?.value as? String
    return namedKClassType?.let {
        val type = it.declaration
        if (type.simpleName.asString() != "Unit") {
            KoinMetaData.Named.ClassNamed(type)
        } else null
    }
            ?: namedStringType?.let { KoinMetaData.Named.StringNamed(it) }
            ?: error("Named annotation needs parameters: either type value or name")
}

fun List<KSValueArgument>.getQualifier(): KoinMetaData.Qualifier {
    val qualifierKClassType: KSType? = firstOrNull { it.name?.asString() == "value" }?.value as? KSType
    val qualifierStringType: String? = firstOrNull { it.name?.asString() == "name" }?.value as? String
    return qualifierKClassType?.let {
        val type = it.declaration
        if (type.simpleName.asString() != "Unit") {
            KoinMetaData.Qualifier.ClassQualifier(type)
        } else null
    }
            ?: qualifierStringType?.let { KoinMetaData.Qualifier.StringQualifier(it) }
            ?: error("Qualifier annotation needs parameters: either type value or name")
}

private val qualifierAnnotations = listOf(Named::class.simpleName, Qualifier::class.simpleName)

fun KSAnnotation.hasQualifier(): Boolean {
    val annotationName = shortName.asString()
    return annotationName in qualifierAnnotations || annotationType.resolve().isCustomQualifierAnnotation()
}

fun KSAnnotation.getQualifier(): String? {
    return when(shortName.asString()){
        "${Named::class.simpleName}" -> arguments.getNamed().getValue()
        "${Qualifier::class.simpleName}" -> arguments.getQualifier().getValue()
        else -> annotationType.resolve().declaration.qualifiedName?.asString()
    }
}

fun KSAnnotated.getQualifier(): String? {
    val qualifierAnnotation = annotations.firstOrNull { it.hasQualifier() }
    return qualifierAnnotation?.getQualifier()
}

fun List<KSValueParameter>.getParameters(): List<KoinMetaData.DefinitionParameter> {
    return map { param -> getParameter(param) }
}

private fun getParameter(param: KSValueParameter): KoinMetaData.DefinitionParameter {
    // Get the first annotation that is not a Provided annotation
    // The [Provided] annotation will be evaluated when making the dependency graph.
    val annotations = param.annotations
    val firstAnnotation = annotations.filter { it.shortName.asString() != Provided::class.simpleName }.firstOrNull()
    val firstAnnotationName = firstAnnotation?.shortName?.asString()
    val firstAnnotationValue = firstAnnotation?.arguments?.getValueArgument()

    val paramName = param.name?.asString()
    val resolvedType: KSType = param.type.resolve()
    val isNullable = resolvedType.isMarkedNullable
    val hasDefault = param.hasDefault
    val resolvedTypeString = resolvedType.toString()

    val isList = resolvedTypeString.startsWith("List<")
    val isLazy = resolvedTypeString.startsWith("Lazy<")

    return when (firstAnnotationName) {
        "${InjectedParam::class.simpleName}" -> KoinMetaData.DefinitionParameter.ParameterInject(name = paramName, isNullable = isNullable, hasDefault = hasDefault,type = resolvedType)
        "${Property::class.simpleName}" -> KoinMetaData.DefinitionParameter.Property(name = paramName, value = firstAnnotationValue, isNullable, hasDefault = hasDefault,type = resolvedType)
        else -> {
            val kind = when {
                isList -> KoinMetaData.DependencyKind.List
                isLazy -> KoinMetaData.DependencyKind.Lazy
                else -> KoinMetaData.DependencyKind.SingleValue
            }
            val qualifierAnnotation = annotations.firstOrNull { it.hasQualifier() }
            val qualifier = qualifierAnnotation?.getQualifier()

            val scopeAnnotation = annotations.firstOrNull { it.shortName.asString() == ScopeId::class.simpleName }
            val scopeIdValue = scopeAnnotation?.arguments?.getScope()?.getValue()

            val isKoinScope = resolvedType.declaration.qualifiedName?.asString() == "org.koin.core.scope.Scope"

            KoinMetaData.DefinitionParameter.Dependency(name = paramName, qualifier = if (!isList) qualifier else null, hasDefault = hasDefault, kind = kind, isNullable = isNullable, type = resolvedType, alreadyProvided = hasProvidedAnnotation(param), scopeId = scopeIdValue, isKoinScope = isKoinScope)
        }
    }
}

private fun KSType.isCustomQualifierAnnotation(): Boolean {
    return (declaration as KSClassDeclaration).annotations.any { it.shortName.asString() in qualifierAnnotations }
}

internal fun List<KSValueArgument>.getValueArgument(): String? {
    return firstOrNull { a -> a.name?.asString() == "value" }?.value as? String?
}

internal fun List<KSValueArgument>.getScopeArgument(): String? {
    return firstOrNull { a -> a.name?.asString() == "scope" }?.value as? String?
}

internal fun List<KSValueArgument>.getArgument(name : String): String? {
    return firstOrNull { a -> a.name?.asString() == name }?.value as? String?
}

fun KSClassDeclaration.getPackageName() : String = packageName.asString()


fun String.filterForbiddenKeywords() : String{
    return split(".").joinToString(".") {
        if (it in forbiddenKeywords) "`$it`" else it
    }
}

private fun hasProvidedAnnotation(param: KSValueParameter): Boolean {
    return param.annotations.any { it.shortName.asString() == Provided::class.simpleName }
}
