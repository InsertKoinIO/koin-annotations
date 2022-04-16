/*
 * Copyright 2017-2022 the original author or authors.
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
import org.koin.compiler.generator.*
import org.koin.compiler.metadata.KoinMetaData
import java.io.OutputStream

fun OutputStream.generateFieldDefaultModule(definitions: List<KoinMetaData.Definition>) {

    definitions.generateImports()
//    KoinGenerator.LOGGER.logging("- generate field definitions: $definitions ...")
    val classDefinitions = definitions.filterIsInstance<KoinMetaData.Definition.ClassDefinition>()
    // TODO optimize group/split?
    val standardDefinitions = classDefinitions.filter { it.isNotScoped() }.toSet()
    val scopeDefinitions = classDefinitions.filter { it.isScoped() }.toSet()

    standardDefinitions.forEach { def -> generateClassDeclarationDefinition(def) }

    scopeDefinitions
        .groupBy { it.scope }
        .forEach { (scope, definitions) ->
            appendText(generateScope(scope!!))
            definitions.forEach { generateClassDeclarationDefinition(it) }
            appendText(generateScopeClosing())
        }
}

fun generateClassModule(classFile: OutputStream, module: KoinMetaData.Module) {
    val generatedField = "${module.name}Module"
    val classModule = "${module.packageName}.${module.name}"

    fun OutputStream.appendAnnotation() {
        appendText("""
            @file:JvmName("${module.name}Gen")
            @file:JvmMultifileClass
        """.trimIndent())
        appendText("\n\n")
    }

    fun OutputStream.appendHeader() {
        appendText(MODULE_HEADER)
    }

    fun OutputStream.appendDefinitionImports() {
//      if (module.definitions.any { it.scope != null }) {
//          classFile.appendText(MODULE_HEADER_STRING_QUALIFIER)
//      }
        appendText(module.definitions.generateImports())
    }
    fun OutputStream.appendModule() {
        appendText("\nprivate val $generatedField = module {")

        if (module.includes?.isNotEmpty() == true) {
            KoinGenerator.LOGGER.logging("generate - includes")
            val generatedIncludes: String = module.includes.generateModuleIncludes()
            appendText("${defaultSpace}includes($generatedIncludes)")
        }

        if (module.definitions.any { it is KoinMetaData.Definition.FunctionDefinition }) {
            appendText("${defaultSpace}val moduleInstance = $classModule()")
        }

        val standardDefinitions = module.definitions.filter { it.isNotScoped() }

        KoinGenerator.LOGGER.logging("generate - definitions")

        standardDefinitions.forEach {
            when (it) {
                is KoinMetaData.Definition.FunctionDefinition -> generateFunctionDeclarationDefinition(it)
                is KoinMetaData.Definition.ClassDefinition -> generateClassDeclarationDefinition(it)
            }
        }

        KoinGenerator.LOGGER.logging("generate - scopes")
        val scopeDefinitions = module.definitions.filter { it.isScoped() }
        scopeDefinitions
            .groupBy { it.scope }
            .forEach { (scope, definitions) ->
                KoinGenerator.LOGGER.logging("generate - scope $scope")
                appendText(generateScope(scope!!))
                definitions.forEach {
                    when (it) {
                        is KoinMetaData.Definition.FunctionDefinition -> generateFunctionDeclarationDefinition(it)
                        is KoinMetaData.Definition.ClassDefinition -> generateClassDeclarationDefinition(it)
                    }
                }
                // close scope
                appendText("\n\t\t\t\t}")
            }

        appendText("\n}")
    }

    fun OutputStream.appendExtensionFunction() {
        appendText("\nval $classModule.module : org.koin.core.module.Module get() = $generatedField")
    }

    classFile.appendAnnotation()
    classFile.appendHeader()
    classFile.appendDefinitionImports()
    classFile.appendModule()
    classFile.appendExtensionFunction()

    classFile.flush()
    classFile.close()
}

fun KoinGenerator.generateDefaultModuleForDefinitions(
    definitions: List<KoinMetaData.Definition>
) {
    definitions.firstOrNull { _ ->
//        logger.logging("+ generate default module: ${definitions.size} ...")
//        logger.logging("+ generate default module header ...")
        val defaultFile = codeGenerator.getDefaultFile()
        defaultFile.generateDefaultModuleHeader(definitions)
        defaultFile.generateFieldDefaultModule(definitions)
//        logger.logging("+ generate default module header ...")
        defaultFile.generateDefaultModuleFooter()
//        logger.logging("+ generate default module +")
        true
    }
}

fun OutputStream.generateDefaultModuleHeader(definitions: List<KoinMetaData.Definition> = emptyList()) {
    appendText(DEFAULT_MODULE_HEADER)
//    if (definitions.any { it.scope != null }) {
//        appendText(MODULE_HEADER_STRING_QUALIFIER)
//    }
    appendText(definitions.generateImports())
    appendText(DEFAULT_MODULE_FUNCTION)
}

fun OutputStream.generateDefaultModuleFooter() {
    appendText(DEFAULT_MODULE_FOOTER)
}

private fun List<KoinMetaData.Definition>.generateImports(): String {
    val globalImports = mapNotNull { definition -> definition.keyword.import?.let { "import $it" } }.joinToString(separator = "\n", postfix = "\n")
//    val hasQualifier: Boolean = any { definition -> definition.qualifier != null || definition.parameters.any { (it as? KoinMetaData.ConstructorParameter.Dependency)?.value != null } }
//    val qualifierImport = if (hasQualifier) "\n$STRING_QUALIFIER_IMPORT\n" else ""
    return globalImports //+ qualifierImport
}

private fun List<KSDeclaration>.generateModuleIncludes(): String {
    return joinToString { it.generateModuleInclude() }
}

private fun KSDeclaration.generateModuleInclude(): String {
    val packageName: String = containingFile?.packageName?.asString() ?: ""
    val className = simpleName.asString()
    return "$packageName.$className().module"
}
