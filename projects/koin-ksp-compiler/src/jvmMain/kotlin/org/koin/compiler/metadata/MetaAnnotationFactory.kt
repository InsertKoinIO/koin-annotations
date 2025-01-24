package org.koin.compiler.metadata

import org.koin.meta.annotations.MetaDefinition
import org.koin.meta.annotations.MetaModule

object MetaAnnotationFactory {
    private val metaModule = MetaModule::class.simpleName!!
    private val metaDefinition = MetaDefinition::class.simpleName!!

    fun generate(module: KoinMetaData.Module): String {
        val fullpath = module.packageName + "." + module.name
        val includesTags = if (module.includes?.isNotEmpty() == true) {
            module.includes.joinToString("\",\"", prefix = "\"", postfix = "\"") { TagFactory.getTag(it) }
        } else null
        val includesString = includesTags?.let { ", includes=[$it]" } ?: ""
        return """
            @$metaModule("$fullpath"$includesString)
        """.trimIndent()
    }

    fun generate(def: KoinMetaData.Definition): String {
        val fullpath = def.packageName + "." + def.label
        val deps = def.parameters.filterIsInstance<KoinMetaData.DefinitionParameter.Dependency>()

        val depsTags = if (deps.isNotEmpty()) {
            deps
                .filter { !it.alreadyProvided }
                .joinToString("\",\"", prefix = "\"", postfix = "\"") { TagFactory.getTag(it) }
        } else null
        val depsString = depsTags?.let { ", dependencies=[$it]" } ?: ""
        return """
            @$metaDefinition("$fullpath"$depsString)
        """.trimIndent()
    }
}