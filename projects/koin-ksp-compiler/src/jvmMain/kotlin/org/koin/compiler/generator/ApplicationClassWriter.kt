package org.koin.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import org.koin.compiler.metadata.KoinMetaData

class ApplicationClassWriter(
    codeGenerator: CodeGenerator,
    val resolver: Resolver,
    val application: KoinMetaData.Application,
) : AbstractFileWriter(codeGenerator) {

    /*
        @Module
        @Configuration
        MyModule

        @KoinApplication
        MyApp
        - or -
        @KoinApplication(configurations = ["default", "test"], modules = [...])
        MyApp

        -> MyApp.startKoin(% lambda config %) = startKoin(lambda + MyApp.koinConfiguration)
        -> MyApp.koinApplication(% lambda config %) = koinApplication(lambda + MyApp.koinConfiguration)

        -> MyApp.koinConfiguration = {
            => config as module list - active modules config only
            configs(<use modules of configs>)
            => list of modules
            modules(...)
        }
        -> Generate config map static ?
            val MyApp.configurationsMap = hashMapOf<String, List<Module>>(...)
     */

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
            fun org.koin.sample.androidx.di.MyKoinApp.startKoin(ext : KoinAppDeclaration?=null) : KoinApplication {
                return GlobalContext.startKoin(koinConfiguration(ext))
            }
        """.trimIndent())
    }

    private fun writeKoinApplicationFunction() {
        writeln("""
            @KoinApplicationDslMarker
            fun org.koin.sample.androidx.di.MyKoinApp.application(ext : KoinAppDeclaration?=null) : KoinApplication {
                return koinApplication(koinConfiguration(ext))
            }
        """.trimIndent())
    }

}