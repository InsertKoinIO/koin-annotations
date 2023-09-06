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
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.generator.KoinGenerator.Companion.LOGGER
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.SINGLE
import org.koin.compiler.scanner.filterForbiddenKeywords
import java.io.OutputStream

const val NEW_LINE = "\n\t"

fun OutputStream.generateDefinition(def: KoinMetaData.Definition, label: () -> String) {
    LOGGER.logging("generate ${def.label} - $def")
    val param = def.parameters.generateParamFunction()
    val ctor = generateConstructor(def.parameters)
    val binds = generateBindings(def.bindings)
    val qualifier = def.qualifier.generateQualifier()
    val createAtStart = if (def.isType(SINGLE) && def.isCreatedAtStart == true) {
        if (qualifier == "") CREATED_AT_START else ",$CREATED_AT_START"
    } else ""
    val space = if (def.isScoped()) NEW_LINE + "\t" else NEW_LINE
    appendText("$space${def.keyword.keyword}($qualifier$createAtStart) { ${param}${label()}$ctor } $binds")
}

fun OutputStream.generateModuleFunctionDeclarationDefinition(def: KoinMetaData.Definition.FunctionDefinition) {
    generateDefinition(def) { "moduleInstance.${def.functionName}" }
}

fun OutputStream.generateFunctionDeclarationDefinition(def: KoinMetaData.Definition.FunctionDefinition) {
    generateDefinition(def) { "${def.packageName}.${def.functionName}" }
}

fun OutputStream.generateClassDeclarationDefinition(def: KoinMetaData.Definition.ClassDefinition) {
    generateDefinition(def) { "${def.packageName}.${def.className}" }
}

const val CREATED_AT_START = "createdAtStart=true"

private fun List<KoinMetaData.ConstructorParameter>.generateParamFunction(): String {
    return if (any { it is KoinMetaData.ConstructorParameter.ParameterInject }) "params -> " else ""
}

private fun String?.generateQualifier(): String = when {
    this == "\"null\"" -> ""
    this == "null" -> ""
    !this.isNullOrBlank() -> "qualifier=org.koin.core.qualifier.StringQualifier(\"$this\")"
    else -> ""
}

val BLOCKED_TYPES = listOf("Any", "ViewModel", "CoroutineWorker", "ListenableWorker")

private fun generateBindings(bindings: List<KSDeclaration>): String {
    val validBindings = bindings.filter {
        val clazzName = it.simpleName.asString()
        clazzName !in BLOCKED_TYPES
    }

    return when {
        validBindings.isEmpty() -> ""
        validBindings.size == 1 -> {
            val generateBinding = generateBinding(validBindings.first())
            "bind($generateBinding)"
        }

        else -> validBindings.joinToString(prefix = "binds(arrayOf(", separator = ",", postfix = "))") {
            generateBinding(it) ?: ""
        }
    }
}

private fun generateBinding(declaration: KSDeclaration): String {
    val packageName = declaration.packageName.asString().filterForbiddenKeywords()
    val className = declaration.simpleName.asString()
    val parents = getParentDeclarations(declaration)
    return if (parents.isNotEmpty()) {
        val parentNames = parents.joinToString(".") { it.simpleName.asString() }
        "$packageName.$parentNames.$className::class"
    } else {
        "$packageName.$className::class"
    }
}

private fun getParentDeclarations(declaration: KSDeclaration): List<KSDeclaration> {
    val parents = mutableListOf<KSDeclaration>()

    var parent = declaration.parentDeclaration
    while (parent != null) {
        parents.add(parent)
        parent = parent.parentDeclaration
    }

    return parents.reversed()
}

fun generateScope(scope: KoinMetaData.Scope): String {
    return when (scope) {
        is KoinMetaData.Scope.ClassScope -> {
            val type = scope.type
            val packageName = type.packageName.asString().filterForbiddenKeywords()
            val className = type.simpleName.asString()
            "${NEW_LINE}scope<$packageName.$className> {"
        }

        is KoinMetaData.Scope.StringScope -> "${NEW_LINE}scope(org.koin.core.qualifier.StringQualifier(\"${scope.name}\")) {\n"
    }
}

fun generateScopeClosing(): String = "${NEW_LINE}}"

private fun generateConstructor(constructorParameters: List<KoinMetaData.ConstructorParameter>): String {
    val paramsWithoutDefaultValues = constructorParameters.filter { !it.hasDefault }
    return paramsWithoutDefaultValues.joinToString(prefix = "(", separator = ",", postfix = ")") { ctorParam ->
        val isNullable: Boolean = ctorParam.nullable
        when (ctorParam) {
            is KoinMetaData.ConstructorParameter.Dependency -> {
                when (ctorParam.kind) {
                    KoinMetaData.DependencyKind.List -> "getAll()"
                    else -> {
                        val keyword = if (ctorParam.kind == KoinMetaData.DependencyKind.Lazy) "inject" else "get"
                        val qualifier =
                            ctorParam.value?.let { "qualifier=org.koin.core.qualifier.StringQualifier(\"${it}\")" }
                                ?: ""
                        val operator = if (!isNullable) "$keyword($qualifier)" else "${keyword}OrNull($qualifier)"
                        if (ctorParam.name == null) operator else "${ctorParam.name}=$operator"
                    }
                }
            }

            is KoinMetaData.ConstructorParameter.ParameterInject -> if (!isNullable) "params.get()" else "params.getOrNull()"
            is KoinMetaData.ConstructorParameter.Property -> if (!isNullable) "getProperty(\"${ctorParam.value}\")" else "getPropertyOrNull(\"${ctorParam.value}\")"
        }
    }
}
