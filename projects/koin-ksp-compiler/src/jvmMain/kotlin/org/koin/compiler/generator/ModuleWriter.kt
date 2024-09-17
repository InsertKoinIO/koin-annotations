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
package org.koin.compiler.generator

import org.koin.compiler.generator.ext.appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import org.koin.compiler.generator.DefinitionWriter.Companion.CREATED_AT_START
import org.koin.compiler.generator.DefinitionWriter.Companion.TAB
import org.koin.compiler.generator.ext.getNewFile
import org.koin.compiler.metadata.KOIN_VIEWMODEL
import org.koin.compiler.metadata.KOIN_VIEWMODEL_COMPOSE
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.scanner.ext.filterForbiddenKeywords
import org.koin.compiler.generator.ext.toSourceString
import java.io.OutputStream

abstract class ModuleWriter(
    val codeGenerator: CodeGenerator,
    val resolver: Resolver,
    val module: KoinMetaData.Module,
) {
    abstract val fileName: String

    open val hasExternalDefinitions: Boolean = false
    open val generateModuleBody: Boolean = true

    private fun createFileStream(): OutputStream = codeGenerator.getNewFile(fileName = fileName)
    private var fileStream: OutputStream? = null
    protected fun write(s: String) { fileStream?.appendText(s) }
    protected fun writeln(s: String) = write("$s\n")
    protected fun writeEmptyLine() = writeln("")
    private lateinit var definitionFactory : DefinitionWriterFactory

    private val modulePath = if (hasEmptyPackage()) module.name else "${module.packageName}.${module.name}"
    private val generatedField = "${module.packageName("_")}_${module.name}"

    //TODO Remove isComposeViewModelActive with Koin 4
    fun writeModule(isComposeViewModelActive: Boolean) {
        fileStream = createFileStream()
        definitionFactory = DefinitionWriterFactory(resolver, fileStream!!)

        writeHeader()
        writeHeaderImports(isComposeViewModelActive)
        if (hasEmptyPackage()) {
            writeln("import ${module.name}")
        }

        if (hasExternalDefinitions) {
            writeExternalDefinitionImports()
            writeExternalDefinitions()
        }

        writeEmptyLine()

        if (module.isExpect){
            writeModuleFooter(closeBrackets = false)

        } else if (generateModuleBody){
            writeModuleFunction()
            writeModuleInstance()
            writeModuleIncludes()
            if (!hasExternalDefinitions) {
                writeDefinitions()
            }
            writeExternalDefinitionCalls()
            writeModuleFooter()
        }

        onFinishWriteModule()
    }

    private fun writeExternalDefinitionImports() {
        writeln("""
            import org.koin.core.annotation.Definition
            import org.koin.core.definition.KoinDefinition
        """.trimIndent())
    }

    open fun writeHeader() {
        writeln(MODULE_HEADER)
    }

    open fun writeHeaderImports(isComposeViewModelActive: Boolean) {
        writeln(generateImports(module.definitions, isComposeViewModelActive))
    }

    private fun generateImports(
        definitions: List<KoinMetaData.Definition>,
        isComposeViewModelActive: Boolean
    ): String {
        return definitions.map { definition -> definition.keyword }
            .toSet()
            .mapNotNull { keyword ->
                if (isComposeViewModelActive && keyword == KOIN_VIEWMODEL) {
                    KOIN_VIEWMODEL_COMPOSE.import.let { "import $it" }
                } else {
                    keyword.import?.let { "import $it" }
                }
            }
            .joinToString(separator = "\n")
    }

    open fun writeExternalDefinitions() {
        //Reduce external defs to ClassDefinition for now
        val standardDefinitions = module.definitions.filter { it.isNotScoped() }
        standardDefinitions.forEach { definitionFactory.writeDefinition(it, module, isExternal = true) }
    }

    open fun writeModuleFunction() {
        // Module definition
        // TODO generate Extensions
        writeln(generateModuleField(module))
    }

    open fun writeModuleInstance() {
        // if any definition is a class function, we need to instantiate the module instance
        // to able to call the function on this instance.
        val needModuleInstance = module
            .definitions.any { it is KoinMetaData.Definition.FunctionDefinition && it.isClassFunction }
                && !module.type.isObject

        if (needModuleInstance) {
            writeln("${TAB}val $MODULE_INSTANCE = $modulePath()")
        }
    }

    private fun generateModuleField(
        module: KoinMetaData.Module
    ): String {
        with(module) {
            val visibilityString = visibility.toSourceString()
            val createdAtStartString = if (isCreatedAtStart != null && isCreatedAtStart) "($CREATED_AT_START)" else ""
            return "${visibilityString}val $generatedField : Module get() = module$createdAtStartString {"
        }
    }

    open fun writeModuleIncludes() {
        if (module.includes?.isNotEmpty() == true){
            generateIncludes()?.let { writeln("${TAB}includes($it)") }
        }
    }

    private fun generateIncludes(): String? {
        return module.includes?.joinToString(separator = ",") { "${it.packageName}.${it.className}().module" }
    }

    open fun writeDefinitions() {
        val (standardDefinitions, scopeDefinitions) = module.definitions.partition { it.isNotScoped() }

        standardDefinitions.forEach { definitionFactory.writeDefinition(it, module) }

        scopeDefinitions
            .groupBy { it.scope }
            .forEach { (scope, scopeDefinitions) ->
                scope?.let { writeScope(scope, scopeDefinitions) }
            }
    }

    open fun writeScope(
        scope: KoinMetaData.Scope,
        scopeDefinitions: List<KoinMetaData.Definition>
    ) {
        writeln(generateScopeHeader(scope))
        scopeDefinitions.forEach { definitionFactory.writeDefinition(it, module) }
        writeln(generateScopeFooter())
    }

    internal fun generateScopeHeader(scope: KoinMetaData.Scope): String {
        return when (scope) {
            is KoinMetaData.Scope.ClassScope -> {
                val type = scope.type
                val packageName = type.packageName.asString().filterForbiddenKeywords()
                val className = type.simpleName.asString()
                "${TAB}scope<$packageName.$className> {"
            }

            is KoinMetaData.Scope.StringScope -> "${TAB}scope(org.koin.core.qualifier.StringQualifier(\"${scope.name}\")) {"
        }
    }

    private fun generateScopeFooter(): String = "${TAB}}"

    open fun writeExternalDefinitionCalls() {
        if (module.externalDefinitions.isNotEmpty()){
            writeln(TAB+generateExternalDefinitionCalls())
        }
    }

    private fun generateExternalDefinitionCalls(): String =
        module.externalDefinitions.joinToString(separator = "\n${TAB}") { "${it.name}()" }

    open fun writeModuleFooter(closeBrackets : Boolean = true) {
        if (closeBrackets) {
            writeln(MODULE_FOOTER)
        }

        val visibilityString = module.visibility.toSourceString()
        val actualKeyword = when {
            module.isActual -> "actual "
            module.isExpect -> "expect "
            else -> ""
        }
        val returnedValue = if (!module.isExpect) " get() = $generatedField" else ""
        writeln("${actualKeyword}${visibilityString}val $modulePath.module : org.koin.core.module.Module${returnedValue}")
    }

    open fun onFinishWriteModule() {
        fileStream?.apply{
            flush()
            close()
        }
    }

    companion object {
        val MODULE_INSTANCE = "moduleInstance"
    }

    private fun hasEmptyPackage(): Boolean {
        return module.packageName.isEmpty() && !module.isDefault
    }
}