package org.koin.compiler.metadata.tag

import org.koin.compiler.generator.ext.appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.generator.ext.getNewFile
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.MetaAnnotationFactory
import org.koin.compiler.resolver.tagAlreadyExists
import org.koin.compiler.resolver.tagPropAlreadyExists
import org.koin.compiler.type.fullWhiteList
import org.koin.compiler.verify.*
import java.io.OutputStream
import java.security.MessageDigest

const val TAG_PREFIX = "_KSP_"
// Avoid looooong name with full SHA as file name. Let's take first digits
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
        default: KoinMetaData.Module,
        applications: List<KoinMetaData.Application>
    ) {
        val isAlreadyGenerated = codeGenerator.generatedFile.isEmpty()
        if (!isAlreadyGenerated) {
            logger.logging("Koin Tags Generation ...")
            createMetaTags(moduleList, default, applications)
        }
    }

    /**
     * To realize [reproducible-builds](https://reproducible-builds.org/), write everything to a temporal file
     * then copy it to the tag file.
     * By this method, we can compute the digest of tag file and use it to name it.
     *
     * @author Kengo TODA
     * @author Arnaud Giuliani
     */
    //TODO Check KoinMeta is constant/reproducible build
    @OptIn(ExperimentalStdlibApi::class)
    private fun createMetaTags(
        moduleList: List<KoinMetaData.Module>,
        default: KoinMetaData.Module,
        applications: List<KoinMetaData.Application>,
    ) {
        val allModules = moduleList.sortedBy { it.name }
        val allDefinitions = (allModules + default).flatMap { it.definitions }.sortedBy { it.label }
        
        // Generate deterministic hash from sorted content
        val contentBuilder = StringBuilder()
        allModules.forEach { contentBuilder.append(it.name) }
        allDefinitions.forEach { contentBuilder.append(it.label) }
        applications.forEach { contentBuilder.append(it.name) }
        val hashString = hashContent(contentBuilder.toString())

        val tagFileName = "KoinMeta-$hashString"

        writeTagFile(tagFileName).buffered().use {
            _tagFileStream = it
            if (isConfigCheckActive){
                writeImports()
            }
            allModules.forEach { module ->
                writeModuleTag(module)
                writeDefinitionsTags(module.definitions, module)
            }
            writeDefinitionsTags(default.definitions, default)
            applications.forEach { application -> writeApplicationTag(application) }
        }
    }

    private fun writeApplicationTag(application: KoinMetaData.Application) {
        if (application.alreadyGenerated == null){
            application.alreadyGenerated = resolver.tagAlreadyExists(application)
        }

        if (application.alreadyGenerated == false){
            val tag = TagFactory.generateTag(application)
            if (tag !in alreadyDeclaredTags) {
                if (isConfigCheckActive){
                    val metaLine = MetaAnnotationFactory.generate(application)
                    writeMeta(metaLine)
                }
                writeTag(tag)
            }
        }
    }

    private fun writeDefinitionsTags(
        allDefinitions: List<KoinMetaData.Definition>,
        module: KoinMetaData.Module,
    ) {
        allDefinitions.forEach { def -> writeDefinitionAndBindingsTags(def, module) }
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
            module.alreadyGenerated = resolver.tagAlreadyExists(module)
        }

        if (module.alreadyGenerated == false){
            val tag = TagFactory.generateTag(module)
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
        module: KoinMetaData.Module,
    ) {
        writeDefinitionTag(def, module)
        def.bindings.forEach { writeBindingTag(def,module, it) }
        if (def.isScoped() && def.scope is KoinMetaData.Scope.ClassScope){
            writeScopeTag(def.scope.type)
        }
    }

    private fun writeScopeTag(
        scope: KSDeclaration
    ) {
        val scopeName = scope.qualifiedName?.asString()
        if (scopeName !in fullWhiteList) {
            val tag = TagFactory.generateTag(scope)
            val alreadyGenerated = resolver.tagPropAlreadyExists(tag)
            if (tag !in alreadyDeclaredTags && !alreadyGenerated) {
                writeTag(tag, asProperty = true)
            }
        }
    }

    private fun writeDefinitionTag(
        definition: KoinMetaData.Definition,
        module: KoinMetaData.Module
    ) {
        if (definition.alreadyGenerated == null){
            definition.alreadyGenerated = resolver.tagAlreadyExists(definition)
        }

        if (definition.alreadyGenerated == false){
            val tag = TagFactory.generateTag(definition)
            if (tag !in alreadyDeclaredTags) {
                if (isConfigCheckActive){
                    val metaLine = MetaAnnotationFactory.generate(definition, module)
                    writeMeta(metaLine)
                }
                writeTag(tag)
            }
        }
    }

    private fun writeBindingTag(
        def: KoinMetaData.Definition,
        module: KoinMetaData.Module,
        binding: KSDeclaration
    ) {
        val name = binding.qualifiedName?.asString()
        if (name !in fullWhiteList) {
            val tag = TagFactory.generateTag(def, binding)
            val alreadyGenerated = resolver.tagPropAlreadyExists(tag)
            if (tag !in alreadyDeclaredTags && !alreadyGenerated) {
                if (isConfigCheckActive){
                    val metaLine = MetaAnnotationFactory.generate(def, module)
                    writeMeta(metaLine)
                }
                writeTag(tag, asProperty = true)
            }
        }
    }

    private fun writeTag(
        tag: String,
        asProperty : Boolean = false,
    ) {
        val line = prepareTagLine(tag,asProperty)
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
            
            import org.koin.meta.annotations.*
        """.trimIndent())
    }

    // Compat with KSP1
    //TODO change for property once KSP2
    private fun prepareTagLine(tagName: String, asFunction: Boolean) : String {
        val cleanedTag = tagName.replace("-", "_")//TODO Check for other rules if needed
        return if (asFunction){
            "\npublic fun $TAG_PREFIX$cleanedTag() : Unit = Unit"
        } else "\npublic class $TAG_PREFIX$cleanedTag"
    }

    companion object {
        val sha1 = MessageDigest.getInstance("SHA1")
        fun hashContent(content : String): String {
            val hash = sha1.digest(content.toByteArray(Charsets.UTF_8))
            val hashString = hash.joinToString("") { "%02x".format(it) }
                .take(TAG_FILE_HASH_LIMIT)
            return hashString
        }
    }
}
