package org.koin.compiler.metadata.tag

import org.koin.compiler.generator.ext.appendText
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.generator.GenerationConfig
import org.koin.compiler.generator.ext.getNewFile
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.MetaAnnotationFactory
import org.koin.compiler.type.fullWhiteList
import java.io.OutputStream
import java.security.MessageDigest

// Avoid looooong name with full SHA as file name. Let's take first digits
private const val TAG_FILE_HASH_LIMIT = 8

class KoinTagWriter(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
    val resolver: TagResolver,
    val isConfigCheckActive : Boolean
) {
    private val alreadyDeclaredTags: MutableSet<String> = mutableSetOf()
    private var _tagFileStream : OutputStream? = null
    private val fileStream : OutputStream
        get() = _tagFileStream ?: error("KoinTagWriter - tagFileStream is null")
    private val pendingContent = StringBuilder()

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
        
        // Batch check already generated status for all items at once
        batchCheckAlreadyGenerated(allModules, allDefinitions, applications)
        
        // Generate deterministic hash from pre-sorted content
        val hashString = generateContentHash(allModules, allDefinitions, applications)
        val tagFileName = "KoinMeta-$hashString"

        writeTagFile(tagFileName).buffered().use {
            _tagFileStream = it
            
            // Build all content in memory first for more efficient I/O
            if (isConfigCheckActive) {
                pendingContent.append("""
                    
                    import org.koin.meta.annotations.*
                """.trimIndent())
            }
            
            // Process all modules and their definitions
            allModules.forEach { module ->
                processModuleAndDefinitions(module)
            }
            processDefinitions(default.definitions, default)
            
            // Process applications
            applications.forEach { application ->
                processApplication(application)
            }
            
            // Write all content in one batch
            fileStream.appendText(pendingContent.toString())
        }
    }

    private fun writeApplicationTag(application: KoinMetaData.Application) {
        if (application.alreadyGenerated == null){
            application.alreadyGenerated = resolver.tagExists(application)
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
        fileStream.appendText("package ${GenerationConfig.getGenerationPath()}\n")
        return fileStream
    }

    private fun writeModuleTag(
        module: KoinMetaData.Module
    ) {
        if (module.alreadyGenerated == null){
            module.alreadyGenerated = resolver.tagExists(module)
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
            writeScopeTag(def.scope)
        }
    }

    private fun writeScopeTag(
        scope: KoinMetaData.Scope.ClassScope
    ) {
        val scopeName = scope.type.qualifiedName?.asString()
        if (scopeName !in fullWhiteList) {
            val tag = TagFactory.generateTag(scope)
            val alreadyGenerated = resolver.tagPropertyExists(tag)
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
            definition.alreadyGenerated = resolver.tagExists(definition)
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
            val alreadyGenerated = resolver.tagPropertyExists(tag)
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

    private fun prepareTagLine(tagName: String, asProperty: Boolean) : String {
        val cleanedTag = TagFactory.prefixTag(tagName.replace("-", "_"), withGenPackage = false)
        return if (asProperty){
            "\npublic val $cleanedTag : Unit get() = Unit"
        } else "\npublic class $cleanedTag"
    }

    // Compat with KSP1
    //TODO change for property once KSP2
//    private fun prepareTagLine(tagName: String, asFunction: Boolean) : String {
//        val cleanedTag = tagName.replace("-", "_")
//        return if (asFunction){
//            "\npublic fun $TAG_PREFIX$cleanedTag() : Unit = Unit"
//        } else "\npublic class $TAG_PREFIX$cleanedTag"
//    }

    private fun batchCheckAlreadyGenerated(
        modules: List<KoinMetaData.Module>,
        definitions: List<KoinMetaData.Definition>,
        applications: List<KoinMetaData.Application>
    ) {
        resolver.batchCheckTagsExist(modules, definitions, applications)
        
        modules.forEach { module ->
            if (module.alreadyGenerated == null) {
                module.alreadyGenerated = resolver.tagExists(module)
            }
        }
        
        definitions.forEach { definition ->
            if (definition.alreadyGenerated == null) {
                definition.alreadyGenerated = resolver.tagExists(definition)
            }
        }
        
        applications.forEach { application ->
            if (application.alreadyGenerated == null) {
                application.alreadyGenerated = resolver.tagExists(application)
            }
        }
    }

    private fun generateContentHash(
        modules: List<KoinMetaData.Module>,
        definitions: List<KoinMetaData.Definition>,
        applications: List<KoinMetaData.Application>
    ): String {
        val contentBuilder = StringBuilder(
            modules.size * 20 + definitions.size * 30 + applications.size * 20
        )
        
        modules.forEach { contentBuilder.append(it.name) }
        definitions.forEach { contentBuilder.append(it.label) }
        applications.forEach { contentBuilder.append(it.name) }
        
        return hashContent(contentBuilder.toString())
    }

    private fun processModuleAndDefinitions(module: KoinMetaData.Module) {
        if (module.alreadyGenerated == false) {
            val tag = TagFactory.generateTag(module)
            if (tag !in alreadyDeclaredTags) {
                if (isConfigCheckActive) {
                    val metaLine = MetaAnnotationFactory.generate(module)
                    pendingContent.append("\n$metaLine")
                }
                addTagContent(tag)
            }
        }
        
        processDefinitions(module.definitions, module)
    }

    private fun processDefinitions(definitions: List<KoinMetaData.Definition>, module: KoinMetaData.Module) {
        definitions.forEach { def ->
            processDefinitionAndBindings(def, module)
        }
    }

    private fun processDefinitionAndBindings(def: KoinMetaData.Definition, module: KoinMetaData.Module) {
        if (def.alreadyGenerated == false) {
            val tag = TagFactory.generateTag(def)
            if (tag !in alreadyDeclaredTags) {
                if (isConfigCheckActive) {
                    val metaLine = MetaAnnotationFactory.generate(def, module)
                    pendingContent.append("\n$metaLine")
                }
                addTagContent(tag)
            }
        }
        
        def.bindings.forEach { binding ->
            val name = binding.qualifiedName?.asString()
            if (name !in fullWhiteList) {
                val tag = TagFactory.generateTag(def, binding)
                val alreadyGenerated = resolver.tagPropertyExists(tag)
                if (tag !in alreadyDeclaredTags && !alreadyGenerated) {
                    if (isConfigCheckActive) {
                        val metaLine = MetaAnnotationFactory.generate(def, module)
                        pendingContent.append("\n$metaLine")
                    }
                    addTagContent(tag, asProperty = true)
                }
            }
        }
        
        if (def.isScoped() && def.scope is KoinMetaData.Scope.ClassScope) {
            val scopeName = def.scope.type.qualifiedName?.asString()
            if (scopeName !in fullWhiteList) {
                val tag = TagFactory.generateTag(def.scope)
                val alreadyGenerated = resolver.tagPropertyExists(tag)
                if (tag !in alreadyDeclaredTags && !alreadyGenerated) {
                    addTagContent(tag, asProperty = true)
                }
            }
        }
    }

    private fun processApplication(application: KoinMetaData.Application) {
        if (application.alreadyGenerated == false) {
            val tag = TagFactory.generateTag(application)
            if (tag !in alreadyDeclaredTags) {
                if (isConfigCheckActive) {
                    val metaLine = MetaAnnotationFactory.generate(application)
                    pendingContent.append("\n$metaLine")
                }
                addTagContent(tag)
            }
        }
    }

    private fun addTagContent(tag: String, asProperty: Boolean = false) {
        val line = prepareTagLine(tag, asProperty)
        pendingContent.append(line)
        alreadyDeclaredTags.add(tag)
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
