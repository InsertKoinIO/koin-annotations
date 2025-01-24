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

class KoinTagWriter(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val resolver: Resolver,
    val isConfigCheckActive : Boolean
) {
    private val alreadyDeclaredTags: ArrayList<String> = arrayListOf()
    private var _tagFileStream : OutputStream? = null
    private val fileStream : OutputStream
        get() = _tagFileStream ?: error("KoinTagWriter - tagFileStream is null")

    fun writeAllTags(
        moduleList: List<KoinMetaData.Module>,
        default: KoinMetaData.Module
    ) {
        val isAlreadyGenerated = codeGenerator.generatedFile.isEmpty()
        if (!isAlreadyGenerated) {
            logger.logging("Koin Tags Generation ...")
            createTagsForModules(moduleList, default)
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
    ) {
        val allDefinitions = (moduleList + default).flatMap { it.definitions }
        val tempFile = createTempFile("KoinMeta", ".kt")
        val sha256 = MessageDigest.getInstance("SHA-256");
        DigestOutputStream(tempFile.outputStream(), sha256).buffered().use {
            _tagFileStream = it
            if (isConfigCheckActive){
                writeImports()
            }
            writeModuleTags(moduleList)
            writeDefinitionsTags(allDefinitions)
        }

        val tagFileName = "KoinMeta-${sha256.digest().toHexString(HexFormat.Default).take(TAG_FILE_HASH_LIMIT)}"
        writeTagFile(tagFileName).buffered().use { Files.copy(tempFile, it) }
    }

    private fun writeModuleTags(
        allModules: List<KoinMetaData.Module>
    ) {
        allModules.forEach { m -> writeModuleTag(m) }
    }

    private fun writeDefinitionsTags(
        allDefinitions: List<KoinMetaData.Definition>,
    ) {
        allDefinitions.forEach { def -> writeDefinitionAndBindingsTags(def) }
    }

    private fun writeTagFile(tagFileName: String): OutputStream {
        val fileStream = codeGenerator.getNewFile(fileName = tagFileName)
        fileStream.appendText("package $codeGenerationPackage\n")
        return fileStream
    }

    private fun writeModuleTag(
        module: KoinMetaData.Module
    ) {
        if (module.alreadyGenerated == null){
            module.alreadyGenerated = resolver.isAlreadyExisting(module)
        }

        if (module.alreadyGenerated == false){
            val tag = TagFactory.getTag(module)
            if (tag !in alreadyDeclaredTags) {
                if (isConfigCheckActive){
                    val metaLine = MetaAnnotationFactory.generate(module)
                    writeMeta(metaLine)
                }
                writeTag(tag)
            }
        }
    }

    private fun writeDefinitionAndBindingsTags(
        def: KoinMetaData.Definition,
    ) {
        writeDefinitionTag(def)
        def.bindings.forEach { writeBindingTag(it) }
    }

    private fun writeDefinitionTag(
        definition: KoinMetaData.Definition
    ) {
        if (definition.alreadyGenerated == null){
            definition.alreadyGenerated = resolver.isAlreadyExisting(definition)
        }

        if (!definition.isExpect && definition.alreadyGenerated == false){
            val tag = TagFactory.getTag(definition)
            if (tag !in alreadyDeclaredTags) {
                if (isConfigCheckActive){
                    val metaLine = MetaAnnotationFactory.generate(definition)
                    writeMeta(metaLine)
                }
                writeTag(tag)
            }
        }
    }

    private fun writeBindingTag(
        binding: KSDeclaration
    ) {
        val name = binding.qualifiedName?.asString()
        if (name !in typeWhiteList) {
            val tag = TagFactory.getTag(binding)
            val alreadyGenerated = resolver.isAlreadyExisting(tag)
            if (tag !in alreadyDeclaredTags && !alreadyGenerated) {
                writeTag(tag)
            }
        }
    }

    private fun writeTag(
        tag: String
    ) {
        val line = prepareTagLine(tag)
        fileStream.appendText(line)
        alreadyDeclaredTags.add(tag)
    }

    private fun writeMeta(
        meta: String
    ) {
        fileStream.appendText("\n$meta")
    }

    private fun writeImports() {
        fileStream.appendText("""
            
            import org.koin.meta.annotations.MetaDefinition
            import org.koin.meta.annotations.MetaModule
        """.trimIndent())
    }

    private fun prepareTagLine(tagName : String) : String {
        return "\npublic class $TAG_PREFIX$tagName"
    }
}
