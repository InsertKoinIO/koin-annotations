package org.koin.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import org.koin.compiler.generator.ext.toSourceString
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.tag.TagResolver

class ApplicationClassWriter(
    codeGenerator: CodeGenerator,
    val application: KoinMetaData.Application,
) : AbstractFileWriter(codeGenerator) {

    override val fileName : String = generateApplicationFileName(application)
    private fun generateApplicationFileName(m: KoinMetaData.Application): String {
        val extensionName = m.packageName("$")
        return "${m.name}Gen${extensionName}"
    }

    val extensionBase = application.packageName+"."+application.name
    val visibility = application.visibility.toSourceString()

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
        writeln("$visibility val $extensionBase.configurationModules : List<Module> get() = listOf($configurationListString)")
    }

    private fun writeKoinConfigurationField() {
        val moduleIncludes = application.moduleIncludes?.let { "+ listOf(${generateIncludes(it)})" } ?: ""
        val configString = """
            $visibility fun $extensionBase.koinConfiguration(config : KoinAppDeclaration?=null) : KoinAppDeclaration = {
                includes(config)
                modules(configurationModules$moduleIncludes)
            }
        """.trimIndent()

        writeln(configString)
    }

    private fun writeStartKoinFunction() {
        writeln("""
            @KoinApplicationDslMarker
            $visibility fun $extensionBase.startKoin(config : KoinAppDeclaration?=null) : KoinApplication {
                return KoinPlatformTools.defaultContext().startKoin(koinConfiguration(config))
            }
        """.trimIndent())
    }

    private fun writeKoinApplicationFunction() {
        writeln("""
            @KoinApplicationDslMarker
            $visibility fun $extensionBase.koinApplication(config : KoinAppDeclaration?=null) : KoinApplication {
                return org.koin.dsl.koinApplication(koinConfiguration(config))
            }
        """.trimIndent())
    }

}