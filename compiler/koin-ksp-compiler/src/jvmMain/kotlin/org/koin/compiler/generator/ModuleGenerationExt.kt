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
    val classDefinitions = definitions.filterIsInstance<KoinMetaData.Definition.ClassDefinition>()
    val standardDefinitions = classDefinitions.filter { it.isNotScoped() }.toSet()
    val scopeDefinitions = classDefinitions.filter { it.isScoped() }.toSet()

    standardDefinitions.forEach { generateClassDeclarationDefinition(it) }
    scopeDefinitions
        .groupBy { it.scope }
        .forEach { (scope, definitions) ->
            appendText(generateScope(scope!!))
            definitions.forEach { generateClassDeclarationDefinition(it) }
            appendText(generateScopeClosing())
        }
}

fun generateClassModule(classFile: OutputStream, module: KoinMetaData.Module) {
    classFile.appendText(MODULE_HEADER)
    classFile.appendText(module.definitions.generateImports())

    val generatedField = module.generateModuleField(classFile)

    val modulePath = "${module.packageName}.${module.name}"

    module.includes?.let { includes ->
        if (includes.isNotEmpty()) { generateIncludes(includes, classFile) }
    }

    if (module.definitions.any { it is KoinMetaData.Definition.FunctionDefinition }) {
        classFile.appendText("${defaultSpace}val moduleInstance = $modulePath()")
    }

    generateDefinitions(module, classFile)

    classFile.appendText("\n}")
    classFile.appendText("\nval $modulePath.module : org.koin.core.module.Module get() = $generatedField")

    classFile.flush()
    classFile.close()
}

private fun generateDefinitions(
    module: KoinMetaData.Module,
    classFile: OutputStream
) {
    val standardDefinitions = module.definitions.filter { it.isNotScoped() }
    standardDefinitions.forEach { it.generateTargetDefinition(classFile) }

    val scopeDefinitions = module.definitions.filter { it.isScoped() }
    scopeDefinitions
        .groupBy { it.scope }
        .forEach { (scope, definitions) ->
            classFile.appendText(generateScope(scope!!))
            definitions.forEach {
                it.generateTargetDefinition(classFile)
            }
            // close scope
            classFile.appendText("\n\t\t\t\t}")
        }
}

private fun KoinMetaData.Definition.generateTargetDefinition(
    classFile: OutputStream
) {
    when (this) {
        is KoinMetaData.Definition.FunctionDefinition -> classFile.generateFunctionDeclarationDefinition(this)
        is KoinMetaData.Definition.ClassDefinition -> classFile.generateClassDeclarationDefinition(this)
    }
}

private fun generateIncludes(
    includeList: List<KSDeclaration>,
    classFile: OutputStream
) {
    val generatedIncludes: String = includeList.generateModuleIncludes()
    classFile.appendText("${defaultSpace}includes($generatedIncludes)")
}

private fun KoinMetaData.Module.generateModuleField(
    classFile: OutputStream
): String {
    val packageName = packageName("_")
    val generatedField = "${packageName}_${name}"
    classFile.appendText("\nval $generatedField = module {")
    return generatedField
}

fun OutputStream.generateDefaultModuleHeader(definitions: List<KoinMetaData.Definition> = emptyList()) {
    appendText(DEFAULT_MODULE_HEADER)
    appendText(definitions.generateImports())
    appendText(DEFAULT_MODULE_FUNCTION)
}

fun OutputStream.generateDefaultModuleFooter() {
    appendText(DEFAULT_MODULE_FOOTER)
}

private fun List<KoinMetaData.Definition>.generateImports(): String {
    return mapNotNull { definition -> definition.keyword.import?.let { "import $it" } }
        .joinToString(separator = "\n", postfix = "\n")
}

private fun List<KSDeclaration>.generateModuleIncludes(): String {
    return joinToString { it.generateModuleInclude() }
}

private fun KSDeclaration.generateModuleInclude(): String {
    val packageName: String = containingFile?.packageName?.asString() ?: ""
    val className = simpleName.asString()
    return "$packageName.$className().module"
}
