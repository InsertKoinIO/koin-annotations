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

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Visibility
import java.util.*

typealias PackageName = String
fun PackageName.camelCase() = split(".").joinToString("") { it.capitalize() }

sealed class KoinMetaData {

    data class Module(
        val packageName: PackageName,
        val name: String,
        val definitions: MutableList<Definition> = mutableListOf(),
        val externalDefinitions: MutableList<ExternalDefinition> = mutableListOf(),
        val type: ModuleType = ModuleType.FIELD,
        val componentsScan: Set<ComponentScan> = emptySet(),
        val includes: List<ModuleInclude>? = null,
        val isCreatedAtStart: Boolean? = null,
        val visibility: Visibility = Visibility.PUBLIC,
        val isDefault: Boolean = false,
        val isExpect : Boolean = false,
        val isActual : Boolean = false
    ) : KoinMetaData() {

        var alreadyGenerated : Boolean? = null

        fun packageName(separator: String): String {
            return if (isDefault) ""
            else {
                splitPackage(packageName, separator)
            }
        }

        data class ComponentScan(val packageName: String = "")

        fun acceptDefinition(defPackageName: String): Boolean {
            return componentsScan.any { componentScan ->
                when {
                    componentScan.packageName.isNotEmpty() -> defPackageName.contains(
                        componentScan.packageName,
                        ignoreCase = true
                    )

                    componentScan.packageName.isEmpty() -> defPackageName.contains(packageName, ignoreCase = true)
                    else -> false
                }
            }
        }

        fun setCurrentDefinitionsToExternals() {
            val externals = definitions
                .filter { !it.isExpect }
                .map { ExternalDefinition(it.packageName, "$DEFINE_PREFIX${it.label}") }

            externalDefinitions.addAll(externals)
        }

        companion object {
            const val DEFINE_PREFIX = "define"
        }
    }

    data class ModuleInclude(
        val packageName: PackageName,
        val className : String,
        val isExpect : Boolean,
        val isActual : Boolean
    )

    enum class ModuleType {
        FIELD, CLASS, OBJECT;

        val isObject: Boolean
            get() = this == OBJECT
    }

    sealed class Scope {
        data class ClassScope(val type: KSDeclaration) : Scope()
        data class StringScope(val name: String) : Scope()

        fun getValue(): String {
            return when (this) {
                is StringScope -> name
                is ClassScope -> "${type.packageName.asString()}.${type.simpleName.asString()}"
            }
        }

        fun getTagValue(): String {
            return when (this) {
                is StringScope -> name
                is ClassScope -> "${type.packageName.asString()}.${type.simpleName.asString()}"
            }
        }
    }

    sealed class Named {
        data class ClassNamed(val type: KSDeclaration) : Named()
        data class StringNamed(val name: String) : Named()

        fun getValue(): String {
            return when (this) {
                is StringNamed -> name
                is ClassNamed -> type.getQualifiedName()
            }
        }
    }

    sealed class Qualifier {
        data class ClassQualifier(val type: KSDeclaration) : Qualifier()
        data class StringQualifier(val name: String) : Qualifier()

        fun getValue(): String {
            return when (this) {
                is StringQualifier -> name
                is ClassQualifier -> type.getQualifiedName()
            }
        }
    }

    data class PropertyValue(val id : String, val field : String)

    data class ExternalDefinition(val targetPackage: String,val name: String)

    sealed class Definition(
        val label: String,
        val parameters: List<DefinitionParameter>,
        val packageName: PackageName,
        val qualifier: String? = null,
        val isCreatedAtStart: Boolean? = null,
        val keyword: DefinitionAnnotation,
        val bindings: List<KSDeclaration>,
        val scope: Scope? = null,
        val isExpect : Boolean,
        val isActual : Boolean
    ) : KoinMetaData() {

        var alreadyGenerated : Boolean? = null

        fun isScoped(): Boolean = scope != null
        fun isNotScoped(): Boolean = !isScoped()
        fun isType(keyword: DefinitionAnnotation): Boolean = this.keyword == keyword

        val packageNamePrefix : String = if (packageName.isEmpty()) "" else "${packageName}."

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Definition

            if (label != other.label) return false
            if (packageName != other.packageName) return false
            if (scope != other.scope) return false
            if (isExpect != other.isExpect) return false

            return true
        }

        override fun hashCode(): Int {
            var result = label.hashCode()
            result = 31 * result + packageName.hashCode()
            result = 31 * result + (scope?.hashCode() ?: 0)
            return result
        }

        class FunctionDefinition(
            packageName: String,
            qualifier: String?,
            isCreatedAtStart: Boolean? = null,
            keyword: DefinitionAnnotation,
            val functionName: String,
            parameters: List<DefinitionParameter> = emptyList(),
            bindings: List<KSDeclaration>,
            scope: Scope? = null,
            isExpect : Boolean,
            isActual : Boolean
        ) : Definition(functionName, parameters, packageName, qualifier, isCreatedAtStart, keyword, bindings, scope, isExpect,isActual) {
            var isClassFunction: Boolean = true
        }

        class ClassDefinition(
            packageName: String,
            qualifier: String?,
            isCreatedAtStart: Boolean? = null,
            keyword: DefinitionAnnotation,
            val className: String,
            val constructorParameters: List<DefinitionParameter> = emptyList(),
            bindings: List<KSDeclaration>,
            scope: Scope? = null,
            isExpect : Boolean,
            isActual : Boolean
        ) : Definition(
            className,
            constructorParameters,
            packageName,
            qualifier,
            isCreatedAtStart,
            keyword,
            bindings,
            scope,
            isExpect,
            isActual
        )
    }

    sealed class DefinitionParameter(val nullable: Boolean = false) {

        abstract val name: String?
        abstract val hasDefault: Boolean
        abstract val type: KSType

        data class Dependency(
            override val name: String?,
            val qualifier: String? = null,
            val isNullable: Boolean = false,
            val scopeId: String? = null,
            override val hasDefault: Boolean,
            val alreadyProvided : Boolean = false,
            override val type: KSType,
            val kind: DependencyKind = DependencyKind.SingleValue
        ) : DefinitionParameter(isNullable)

        data class ParameterInject(
            override val name: String?,
            val isNullable: Boolean = false,
            override val hasDefault: Boolean,
            override val type: KSType
        ) : DefinitionParameter(isNullable)

        data class Property(
            override val name: String?,
            val value: String? = null,
            val isNullable: Boolean = false,
            var defaultValue: PropertyValue? = null,
            override val hasDefault: Boolean,
            override val type: KSType
        ) : DefinitionParameter(isNullable)
    }

    enum class DependencyKind {
        SingleValue, List, Lazy
    }
}

internal fun splitPackage(packageName : String, separator: String, default: Locale = Locale.getDefault()) : String =
    packageName.split(".").joinToString(separator) { it.lowercase(default) }


private fun KSDeclaration.getQualifiedName(): String {
    val packageName = packageName.asString()
    val qualifiedName =  qualifiedName?.asString()

    return qualifiedName?.let {
        val kClassName = qualifiedName
            .removePrefix("${packageName}.")
            .replace(".", "\\$")
        "$packageName.$kClassName"
    } ?: run {
        "${this.packageName.asString()}.${simpleName.asString()}"
    }
}
