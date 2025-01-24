package org.koin.compiler.metadata

import org.koin.compiler.generator.ext.appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.generator.ext.getNewFile
import org.koin.compiler.verify.*
import org.koin.compiler.verify.typeWhiteList
import java.io.OutputStream
import java.nio.file.Files
import java.security.DigestOutputStream
import java.security.MessageDigest
import kotlin.io.path.createTempFile
import kotlin.io.path.outputStream

const val TAG_PREFIX = "KoinMeta_"
// Avoid looooong name with full SHA as file name. Let's take 8 first digits
private const val TAG_FILE_HASH_LIMIT = 8

class KoinTagWriter(val codeGenerator: CodeGenerator, val logger: KSPLogger) {

    lateinit var resolver: Resolver

    fun writeAllTags(
        moduleList: List<KoinMetaData.Module>,
        default: KoinMetaData.Module,
        isConfigCheckActive: Boolean
    ) {
        val isAlreadyGenerated = codeGenerator.generatedFile.isEmpty()
        if (!isAlreadyGenerated) {
            logger.logging("Koin Tags Generation ...")
            createTagsForModules(moduleList, default, isConfigCheckActive)
        }
    }

    /**
     * To realize [reproducible-builds](https://reproducible-builds.org/), write everything to a temporal file
     * then copy it to the tag file.
     * By this method, we can compute the digest of tag file and use it to name it.
     *
     * @author Kengo TODA
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun createTagsForModules(
        moduleList: List<KoinMetaData.Module>,
        default: KoinMetaData.Module,
        isConfigCheckActive: Boolean,
    ) {
        val allDefinitions = (moduleList + default).flatMap { it.definitions }
        val tempFile = createTempFile("KoinMeta", ".kt")
        val sha256 = MessageDigest.getInstance("SHA-256");
        DigestOutputStream(tempFile.outputStream(), sha256).buffered().use {
            val alreadyDeclaredTags = arrayListOf<String>()
            writeModuleTags(moduleList, it, alreadyDeclaredTags, isConfigCheckActive)
            writeDefinitionsTags(allDefinitions, it, alreadyDeclaredTags, isConfigCheckActive)
        }

        val tagFileName = "KoinMeta-${sha256.digest().toHexString(HexFormat.Default).take(TAG_FILE_HASH_LIMIT)}"
        writeTagFile(tagFileName).buffered().use { Files.copy(tempFile, it) }
    }

    private fun writeModuleTags(
        allModules: List<KoinMetaData.Module>,
        tagFileStream: OutputStream,
        alreadyDeclaredTags: ArrayList<String>,
        isConfigCheckActive: Boolean
    ) {
        allModules.forEach { m -> writeModuleTag(tagFileStream,m,alreadyDeclaredTags, isConfigCheckActive) }
    }

    private fun writeDefinitionsTags(
        allDefinitions: List<KoinMetaData.Definition>,
        tagFileStream: OutputStream,
        alreadyDeclaredTags: ArrayList<String>,
        isConfigCheckActive: Boolean
    ) {
        allDefinitions.forEach { def -> writeDefinitionAndBindingsTags(tagFileStream, def, alreadyDeclaredTags, isConfigCheckActive) }
    }

    private fun writeTagFile(tagFileName: String): OutputStream {
        val fileStream = codeGenerator.getNewFile(fileName = tagFileName)
        fileStream.appendText("package $codeGenerationPackage\n")
        return fileStream
    }

    private fun writeModuleTag(
        fileStream: OutputStream,
        module: KoinMetaData.Module,
        alreadyDeclaredTags: ArrayList<String>,
        isConfigCheckActive: Boolean
    ) {
        if (module.alreadyGenerated == null){
            module.alreadyGenerated = resolver.isAlreadyExisting(module)
        }

        if (module.alreadyGenerated == false){
            val tag = TagFactory.getTag(module)
            if (tag !in alreadyDeclaredTags) {
                writeTag(tag, fileStream, alreadyDeclaredTags)
            }
        }
    }

    private fun writeDefinitionAndBindingsTags(
        fileStream: OutputStream,
        def: KoinMetaData.Definition,
        alreadyDeclaredTags: ArrayList<String>,
        isConfigCheckActive: Boolean
    ) {
        writeDefinitionTag(def, alreadyDeclaredTags, fileStream, isConfigCheckActive)
        def.bindings.forEach { writeBindingTag(it, alreadyDeclaredTags, fileStream) }
    }

    private fun writeDefinitionTag(
        definition: KoinMetaData.Definition,
        alreadyDeclared: java.util.ArrayList<String>,
        fileStream: OutputStream,
        isConfigCheckActive: Boolean
    ) {
        if (definition.alreadyGenerated == null){
            definition.alreadyGenerated = resolver.isAlreadyExisting(definition)
        }

        if (!definition.isExpect && definition.alreadyGenerated == false){
            val tag = TagFactory.getTag(definition)
            if (tag !in alreadyDeclared) {
                writeTag(tag, fileStream, alreadyDeclared)
            }
        }
    }

    private fun writeBindingTag(
        binding: KSDeclaration,
        alreadyDeclared: ArrayList<String>,
        fileStream: OutputStream
    ) {
        val name = binding.qualifiedName?.asString()
        if (name !in typeWhiteList) {
            val tag = TagFactory.getTag(binding)
            val alreadyGenerated = resolver.isAlreadyExisting(tag)
            if (tag !in alreadyDeclared && !alreadyGenerated) {
                writeTag(tag, fileStream, alreadyDeclared)
            }
        }
    }

    private fun writeTag(
        tag: String,
        fileStream: OutputStream,
        alreadyDeclared: java.util.ArrayList<String>
    ) {
        val line = prepareTagLine(tag)
        fileStream.appendText(line)
        alreadyDeclared.add(tag)
    }

    private fun prepareTagLine(tagName : String) : String {
        return "\npublic class $TAG_PREFIX$tagName"
    }
}
