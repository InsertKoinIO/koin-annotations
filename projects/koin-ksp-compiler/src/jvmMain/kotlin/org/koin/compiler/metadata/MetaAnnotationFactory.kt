package org.koin.compiler.metadata

import org.koin.compiler.verify.typeWhiteList
import org.koin.meta.annotations.MetaDefinition
import org.koin.meta.annotations.MetaModule

object MetaAnnotationFactory {
    private val metaModule = MetaModule::class.simpleName!!
    private val metaDefinition = MetaDefinition::class.simpleName!!
    private val whiteListTags = typeWhiteList.map { TagFactory.getTagFromFullPath(it) }

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
        val dependencies = def.parameters.filterIsInstance<KoinMetaData.DefinitionParameter.Dependency>()

        val cleanedDependencies = dependencies
            .filter { !it.alreadyProvided && !it.hasDefault && !it.isNullable }
            .map { TagFactory.getTag(it) }
            .filter { it !in whiteListTags }

        val depsTags = if (cleanedDependencies.isNotEmpty()) cleanedDependencies.joinToString(
            "\",\"",
            prefix = "\"",
            postfix = "\""
        ) else null

        val depsString = depsTags?.let { ", dependencies=[$it]" } ?: ""

        return """
            @$metaDefinition("$fullpath"$depsString)
        """.trimIndent()
    }
}