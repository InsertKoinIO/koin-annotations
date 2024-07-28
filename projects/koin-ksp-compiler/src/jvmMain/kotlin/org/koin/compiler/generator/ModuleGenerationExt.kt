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
import org.koin.compiler.generator.*
import org.koin.compiler.metadata.KOIN_VIEWMODEL
import org.koin.compiler.metadata.KOIN_VIEWMODEL_COMPOSE
import org.koin.compiler.metadata.KoinMetaData
import java.io.OutputStream

fun OutputStream.generateFieldDefaultModule(
    definitions: List<KoinMetaData.Definition>,
    generateExternalDefinitions: Boolean
) {
    val standardDefinitions = definitions.filter { it.isNotScoped() }.toSet()
    val scopeDefinitions = definitions.filter { it.isScoped() }.toSet()

    standardDefinitions.forEach { generateDefaultModuleDefinition(it, generateExternalDefinitions) }
    //TODO Scope in function?
    scopeDefinitions
        .groupBy { it.scope }
        .forEach { (scope, definitions) ->
            appendText(generateScope(scope!!))
            definitions.forEach { definition ->
                generateDefaultModuleDefinition(definition, generateExternalDefinitions)
            }
            appendText(generateScopeClosing())
        }
}

fun OutputStream.generateDefaultModuleDefinition(
    definition: KoinMetaData.Definition,
    generateExternalDefinitions: Boolean
) {
    if (definition is KoinMetaData.Definition.ClassDefinition) {
        generateClassDeclarationDefinition(definition, isExternalDefinition = generateExternalDefinitions)
    } else if (definition is KoinMetaData.Definition.FunctionDefinition && !definition.isClassFunction) {
        generateFunctionDeclarationDefinition(definition, isExternalDefinition = generateExternalDefinitions)
    }
}

//TODO Remove isComposeViewModelActive once use Koin 4 ViewModel dsl
fun generateClassModule(classFile: OutputStream, module: KoinMetaData.Module, isComposeViewModelActive: Boolean) {
    classFile.appendText(moduleHeader())
    classFile.appendText(module.definitions.generateImports(isComposeViewModelActive))

    val generatedField = module.generateModuleField(classFile)

    val modulePath = "${module.packageName}.${module.name}"

    module.includes?.let { includes ->
        if (includes.isNotEmpty()) {
            generateIncludes(includes, classFile)
        }
    }

    if (module.definitions.isNotEmpty() && module.isExpect.not()) {
        if (module.definitions.any {
                // if any definition is a class function, we need to instantiate the module instance
                // to able to call the function on this instance.
                it is KoinMetaData.Definition.FunctionDefinition &&
                        it.isClassFunction
            } && !module.type.isObject) {
            classFile.appendText("${NEW_LINE}val moduleInstance = $modulePath()")
        }

        generateDefinitions(module, classFile)
    }

    if (module.externalDefinitions.isNotEmpty() && module.isExpect.not()) {
        classFile.generateExternalDefinitionCalls(module)
    }

    if (module.isExpect){
        classFile.appendText("\n// empty module due to isExpect")
    }

    classFile.appendText("\n}")
    val visibilityString = module.visibility.toSourceString()
    classFile.appendText(
        "\n${visibilityString}val $modulePath.module : org.koin.core.module.Module get() = $generatedField"
    )

    classFile.flush()
    classFile.close()
}

private fun generateDefinitions(
    module: KoinMetaData.Module,
    classFile: OutputStream
) {
    val standardDefinitions = module.definitions.filter { it.isNotScoped() }
    standardDefinitions.forEach { it.generateTargetDefinition(module, classFile) }

    val scopeDefinitions = module.definitions.filter { it.isScoped() }
    scopeDefinitions
        .groupBy { it.scope }
        .forEach { (scope, definitions) ->
            classFile.appendText(generateScope(scope!!))
            definitions.forEach {
                it.generateTargetDefinition(module, classFile)
            }
            // close scope
            classFile.appendText("\n\t\t\t\t}")
        }
}

private fun KoinMetaData.Definition.generateTargetDefinition(
    module: KoinMetaData.Module,
    classFile: OutputStream
) {
    when (this) {
        is KoinMetaData.Definition.FunctionDefinition -> {
            if (isClassFunction) {
                if (module.type.isObject) {
                    val modulePath = "${module.packageName}.${module.name}"
                    classFile.generateObjectModuleFunctionDeclarationDefinition(this, modulePath)
                } else {
                    classFile.generateModuleFunctionDeclarationDefinition(this)
                }
            } else {
                classFile.generateFunctionDeclarationDefinition(this)
            }
        }

        is KoinMetaData.Definition.ClassDefinition -> classFile.generateClassDeclarationDefinition(this)
    }
}

private fun generateIncludes(
    includeList: List<KSDeclaration>,
    classFile: OutputStream
) {
    val generatedIncludes: String = includeList.generateModuleIncludes()
    classFile.appendText("${NEW_LINE}includes($generatedIncludes)")
}

private fun KoinMetaData.Module.generateModuleField(
    classFile: OutputStream
): String {
    val packageName = packageName("_")
    val generatedField = "${packageName}_${name}"
    val visibilityString = visibility.toSourceString()
    val createdAtStartString = if (isCreatedAtStart != null && isCreatedAtStart) "($CREATED_AT_START)" else ""
    classFile.appendText("\n${visibilityString}val $generatedField : Module get() = module$createdAtStartString {")
    return generatedField
}

fun OutputStream.generateDefaultModuleHeader(
    definitions: List<KoinMetaData.Definition>,
    isComposeViewModelActive: Boolean
) {
    appendText(DEFAULT_MODULE_HEADER)
    appendText(definitions.generateImports(isComposeViewModelActive))
}

fun OutputStream.generateDefaultModuleFunction() {
    appendText("\n\n")
    appendText(DEFAULT_MODULE_FUNCTION)
}

fun OutputStream.generateDefaultModuleFooter() {
    appendText(DEFAULT_MODULE_FOOTER)
}

//TODO Remove isComposeViewModelActive with Koin 4
private fun List<KoinMetaData.Definition>.generateImports(isComposeViewModelActive: Boolean = false): String {
    return map { definition -> definition.keyword }
        .toSet()
        .mapNotNull { keyword ->
            if (isComposeViewModelActive && keyword == KOIN_VIEWMODEL){
                KOIN_VIEWMODEL_COMPOSE.import.let { "import $it" }
            } else {
                keyword.import?.let { "import $it" }
            }
        }
        .joinToString(separator = "\n", postfix = "\n")
}

private fun List<KSDeclaration>.generateModuleIncludes(): String {
    return joinToString { it.generateModuleInclude() }
}

private fun KSDeclaration.generateModuleInclude(): String {
    val packageName: String = packageName.asString()
    val className = simpleName.asString()
    return "$packageName.$className().module"
}
