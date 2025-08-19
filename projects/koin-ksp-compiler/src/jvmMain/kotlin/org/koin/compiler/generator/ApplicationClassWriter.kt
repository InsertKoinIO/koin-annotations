package org.koin.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import org.koin.compiler.metadata.KoinMetaData

class ApplicationClassWriter(
    codeGenerator: CodeGenerator,
    val resolver: Resolver,
    val application: KoinMetaData.Application,
) : AbstractFileWriter(codeGenerator) {

    override val fileName : String = generateApplicationFileName(application)
    private fun generateApplicationFileName(m: KoinMetaData.Application): String {
        val extensionName = m.packageName("$")
        return "${m.name}Gen${extensionName}"
    }

    val extensionBase = application.packageName+"."+application.name

    fun writeApplication() {
        fileStream = createFileStream()
        writeImports()
        writeConfigurationMap()
        writeKoinConfigurationField()
        writeStartKoinFunction()
        writeKoinApplicationFunction()
    }

    private fun writeImports() {
        writeln(APP_HEADER)
        writeEmptyLine()
    }

    private fun writeConfigurationMap() {
        val configurationListString = application.configurations?.flatMap { configuration -> configuration.modules }?.distinct()?.let { generateIncludes(it) }
        writeln("val $extensionBase.configurationModules : List<Module> get() = listOf($configurationListString)")
    }

    private fun writeKoinConfigurationField() {
        val moduleIncludes = application.moduleIncludes?.let { "+ listOf(${generateIncludes(it)})" } ?: ""
        val configString = """
            fun $extensionBase.koinConfiguration(ext : KoinAppDeclaration?=null) : KoinAppDeclaration = {
                includes(ext)
                modules(configurationModules$moduleIncludes)
            }
        """.trimIndent()

        writeln(configString)
    }

    private fun writeStartKoinFunction() {
        writeln("""
            @KoinApplicationDslMarker
            fun $extensionBase.startKoin(ext : KoinAppDeclaration?=null) : KoinApplication {
                return GlobalContext.startKoin(koinConfiguration(ext))
            }
        """.trimIndent())
    }

    private fun writeKoinApplicationFunction() {
        writeln("""
            @KoinApplicationDslMarker
            fun $extensionBase.koinApplication(ext : KoinAppDeclaration?=null) : KoinApplication {
                return org.koin.dsl.koinApplication(koinConfiguration(ext))
            }
        """.trimIndent())
    }

}