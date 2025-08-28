package org.koin.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.metadata.KoinMetaData

class MonitoredProxyClassWriter(
    codeGenerator: CodeGenerator,
    val resolver: Resolver,
    val definition: KoinMetaData.Definition.ClassDefinition,
) : AbstractFileWriter(codeGenerator) {

    override val fileName : String = generateApplicationFileName(definition)
    private fun generateApplicationFileName(m: KoinMetaData.Definition): String {
        val extensionName = m.packageName("$")
        return "${m.label}Proxy${extensionName}"
    }

    fun writeProxy() {
        fileStream = createFileStream()
        val fullClassName = definition.packageName+"."+definition.className
        val declaration = resolver.getClassDeclarationByName(resolver.getKSNameFromString(fullClassName))
        if (declaration != null){
            writeHeader()

            val bypassed = listOf("toString","equals","hashCode","<init>")

            val primaryConstructor = declaration.primaryConstructor?.parameters
            val ctorTyped = if (primaryConstructor?.isEmpty() == true) "" else primaryConstructor?.joinToString(", ") { "${it.name?.asString()} : ${it.type.resolve().declaration.qualifiedName?.asString()}" }
            val ctorList = if (primaryConstructor?.isEmpty() == true) "" else primaryConstructor?.joinToString(", ") { "${it.name?.asString()}" }
            writeln("class ${definition.className}Proxy($ctorTyped) : ${fullClassName}($ctorList) {")

            writeTraceFunction()

            declaration.getAllFunctions()
                .filter { it.simpleName.asString() !in bypassed }
                .toList()
                .forEach { writeFunction(it) }

            writeln("}")


        } else {
            LOGGER.warn("[DEBUG] Can't create proxy for class '${definition.className}'. Class not found")
        }
    }

    private fun writeFunction(functionDeclaration: KSFunctionDeclaration) {
        val functionName = functionDeclaration.simpleName.asString()
        val functionParam = functionDeclaration.parameters.joinToString(", ") { "${it.name?.asString()} : ${it.type.resolve().declaration.qualifiedName?.asString()}" }
        val functionParamCall = functionDeclaration.parameters.joinToString(", ") { "${it.name?.asString()} " }

        val modifiers = if (functionDeclaration.modifiers.isEmpty()) "" else functionDeclaration.modifiers.joinToString(" ", postfix = " ") { "$it".lowercase() }
        val returnedType = buildReturnTypeString(functionDeclaration)
        val thisString = "\$this"
        writeln("\toverride ${modifiers}fun $functionName($functionParam) : $returnedType { return trace(\"${thisString}.$functionName\") { super.${functionName}($functionParamCall) } }")
    }

    private fun buildReturnTypeString(functionDeclaration: KSFunctionDeclaration): String {
        val returnType = functionDeclaration.returnType?.resolve()
        return if (returnType != null) {
            buildTypeString(returnType)
        } else {
            "Unit"
        }
    }

    private fun buildTypeString(type: com.google.devtools.ksp.symbol.KSType): String {
        val declaration = type.declaration
        val baseTypeName = declaration.qualifiedName?.asString() ?: "Unit"
        val nullSuffix = if (type.isMarkedNullable) "?" else ""
        
        val typeArguments = type.arguments
        val fullTypeName = if (typeArguments.isNotEmpty()) {
            val genericTypes = typeArguments.joinToString(", ") { arg ->
                val resolvedArg = arg.type?.resolve()
                if (resolvedArg != null) {
                    buildTypeString(resolvedArg)
                } else {
                    "*"
                }
            }
            "$baseTypeName<$genericTypes>"
        } else {
            baseTypeName
        }
        return "$fullTypeName$nullSuffix"
    }

    private fun writeTraceFunction() {
        val fName = "\$fName"
        val tDuration = "\${t.duration.inWholeMilliseconds}"
        writeln( """
    final inline fun <reified R> trace(fName: String, func: () -> R): R {
        val t = measureTimedValue(func)
        println("[TRACING] -> $fName in $tDuration")
        return t.value
    }
        """)
    }

    private fun writeHeader() {
        writeln("""
            package org.koin.ksp.generated
            
            import kotlin.time.measureTimedValue
            
        """.trimIndent())
    }
}