package org.koin.compiler.verify

import org.koin.compiler.generator.ext.appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER
import org.koin.compiler.generator.ext.getNewFile
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.verify.ext.getResolution
import org.koin.compiler.verify.ext.getResolutionForTag
import java.io.OutputStream

const val tagPrefix = "KoinDef"

class KoinTagWriter(val codeGenerator: CodeGenerator, val logger: KSPLogger) {

    lateinit var resolver: Resolver

    fun writeAllTags(
        moduleList: List<KoinMetaData.Module>,
        default : KoinMetaData.Module
    ) {

        val isAlreadyGenerated = codeGenerator.generatedFile.isEmpty()
        val allDefinitions = (moduleList + default).flatMap { it.definitions }

        if (!isAlreadyGenerated) {
            logger.logging("Koin Tags Generation ...")
            val tagFileName = "KoinMeta-${hashCode()}"
            val tagFileStream = writeTagFile(tagFileName)

            writeModuleTags(moduleList,tagFileStream)
            writeDefinitionsTags(allDefinitions,tagFileStream)
        }
    }

    private fun writeModuleTags(allModules: List<KoinMetaData.Module>, tagFileStream : OutputStream) {
        val alreadyDeclaredTags = arrayListOf<String>()
        allModules.forEach { m -> writeModuleTag(tagFileStream,m,alreadyDeclaredTags) }
    }

    private fun writeDefinitionsTags(allDefinitions: List<KoinMetaData.Definition>, tagFileStream : OutputStream) {
        val alreadyDeclaredTags = arrayListOf<String>()

        allDefinitions.forEach { def -> writeDefinitionTag(tagFileStream, def, alreadyDeclaredTags) }
    }

    private fun writeTagFile(tagFileName: String): OutputStream {
        val fileStream = codeGenerator.getNewFile(fileName = tagFileName)
        fileStream.appendText("package $codeGenerationPackage\n")
        return fileStream
    }

    private fun writeModuleTag(
        fileStream: OutputStream,
        mod: KoinMetaData.Module,
        alreadyDeclared: ArrayList<String>
    ) {
        logger.logging("writeModuleTag? ${mod.name}")
        if (mod.alreadyGenerated == null){
            mod.alreadyGenerated = resolver.getResolution(mod) != null
        }

        if (mod.alreadyGenerated == false){
            val className = mod.getTagName()
            if (className !in alreadyDeclared) {
                writeTagLine(className, fileStream, alreadyDeclared)
            }
        }
    }

    private fun writeDefinitionTag(
        fileStream: OutputStream,
        def: KoinMetaData.Definition,
        alreadyDeclared: ArrayList<String>
    ) {
        writeClassTag(def, alreadyDeclared, fileStream)
        def.bindings.forEach { writeBindingTag(it, alreadyDeclared, fileStream) }
    }

    private fun writeClassTag(
        def: KoinMetaData.Definition,
        alreadyDeclared: java.util.ArrayList<String>,
        fileStream: OutputStream
    ) {
        if (def.alreadyGenerated == null){
            def.alreadyGenerated = resolver.getResolution(def) != null
        }

        if (!def.isExpect && def.alreadyGenerated == false){
            val className = def.getTagName()
            if (className !in alreadyDeclared) {
                writeTagLine(className, fileStream, alreadyDeclared)
            }
        }
    }

    private fun writeBindingTag(
        binding: KSDeclaration,
        alreadyDeclared: ArrayList<String>,
        fileStream: OutputStream
    ) {
        binding.qualifiedName?.asString()?.let { name ->
            if (name !in typeWhiteList) {
                binding.qualifiedNameCamelCase()?.let { className ->
                    val alreadyGenerated = resolver.getResolutionForTag(className) != null
                    if (className !in alreadyDeclared && !alreadyGenerated) {
                        writeTagLine(className, fileStream, alreadyDeclared)
                    }
                }
            }
        }
    }

    private fun writeTagLine(
        tagName: String,
        fileStream: OutputStream,
        alreadyDeclared: java.util.ArrayList<String>
    ) {
//        LOGGER.logging("tag: $tagName")
        val tag = "public class $tagPrefix$tagName"
        fileStream.appendText("\n$tag")
        alreadyDeclared.add(tagName)
    }
}