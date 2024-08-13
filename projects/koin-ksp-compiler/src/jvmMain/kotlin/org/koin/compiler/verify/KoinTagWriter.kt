package org.koin.compiler.verify

import appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.generator.getNewFile
import org.koin.compiler.metadata.KoinMetaData
import java.io.OutputStream

const val tagPrefix = "KoinDef"

class KoinTagWriter(val codeGenerator: CodeGenerator, val logger: KSPLogger) {

    fun writeTags(allDefinitions: List<KoinMetaData.Definition>) {
        val alreadyDeclaredTags = arrayListOf<String>()
        val tagFileName = "KoinMeta-${hashCode()}"
        val tagFileStream = writeTagFile(tagFileName)

        allDefinitions.forEach { def -> writeDefinitionTag(tagFileStream, def, alreadyDeclaredTags) }
    }

    private fun writeTagFile(tagFileName: String): OutputStream {
        val fileStream = codeGenerator.getNewFile(fileName = tagFileName)
        fileStream.appendText("package $codeGenerationPackage\n")
        return fileStream
    }

    private fun writeDefinitionTag(
        fileStream: OutputStream,
        def: KoinMetaData.Definition,
        alreadyDeclared: ArrayList<String>
    ) {
        writeClassTag(def, alreadyDeclared, fileStream)
        def.bindings.forEach { writeDefinitionBindingTag(it, alreadyDeclared, fileStream) }
    }

    private fun writeClassTag(
        def: KoinMetaData.Definition,
        alreadyDeclared: java.util.ArrayList<String>,
        fileStream: OutputStream
    ) {
        val className = def.packageCamelCase() + def.label.capitalize()
        if (className !in alreadyDeclared) {
            writeTagLine(className, fileStream, alreadyDeclared)
        }
    }

    private fun writeTagLine(
        className: String,
        fileStream: OutputStream,
        alreadyDeclared: java.util.ArrayList<String>
    ) {
        val tag = "public class $tagPrefix$className"
        fileStream.appendText("\n$tag")
        alreadyDeclared.add(className)
    }

    private fun writeDefinitionBindingTag(
        binding: KSDeclaration,
        alreadyDeclared: ArrayList<String>,
        fileStream: OutputStream
    ) {
        val name = binding.qualifiedName?.asString()
        if (name !in typeWhiteList) {
            val className = binding.qualifiedNameCamelCase()
            if (className !in alreadyDeclared && className != null) {
                writeTagLine(className, fileStream, alreadyDeclared)
            }
        }
    }
}