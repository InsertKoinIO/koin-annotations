package org.koin.compiler.metadata

import org.koin.compiler.metadata.KoinMetaData.DependencyKind
import org.koin.meta.annotations.MetaDefinition
import org.koin.meta.annotations.MetaModule

object MetaAnnotationFactory {
    private val metaModule = MetaModule::class.simpleName!!
    private val metaDefinition = MetaDefinition::class.simpleName!!

    fun generate(module: KoinMetaData.Module): String {
        val fullpath = module.packageName + "." + module.name

        val includesTags = if (module.includes?.isNotEmpty() == true) {
            module.includes.joinToString("\",\"", prefix = "\"", postfix = "\"") { TagFactory.getMetaTag(it) }
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
            .mapNotNull { dep ->
                if (dep.kind == DependencyKind.Single) TagFactory.getMetaTag(dep)
                else {
                    val ksDeclaration = extractLazyOrListType(dep)
                    ksDeclaration?.let { TagFactory.getMetaTag(def, dep ,ksDeclaration) }
                }
            }

        val depsTags = if (cleanedDependencies.isNotEmpty()) cleanedDependencies.joinToString(
            "\",\"",
            prefix = "\"",
            postfix = "\""
        ) else null

        val depsString = depsTags?.let { ", dependencies=[$it]" } ?: ""

        val scopeDef = if (def.isScoped()) def.scope?.getTagValue()?.camelCase() else null
        val scopeString = scopeDef?.let { ", scope=\"$it\"" } ?: ""
        return """
            @$metaDefinition("$fullpath"$depsString$scopeString)
        """.trimIndent()
    }

    private fun extractLazyOrListType(it: KoinMetaData.DefinitionParameter.Dependency) =
        it.type.arguments.first().type?.resolve()?.declaration
}