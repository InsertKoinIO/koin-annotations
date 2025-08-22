package org.koin.compiler.metadata

import org.koin.compiler.metadata.KoinMetaData.DependencyKind
import org.koin.compiler.metadata.tag.TagFactory
import org.koin.compiler.type.fullWhiteList
import org.koin.meta.annotations.MetaApplication
import org.koin.meta.annotations.MetaDefinition
import org.koin.meta.annotations.MetaModule

object MetaAnnotationFactory {
    private val metaApplication = MetaApplication::class.simpleName!!
    private val metaModule = MetaModule::class.simpleName!!
    private val metaDefinition = MetaDefinition::class.simpleName!!

    fun generate(application: KoinMetaData.Application): String {
        val fullpath = application.fullpath

        val includesTags = if (application.moduleIncludes?.isNotEmpty() == true) {
            application.moduleIncludes.joinToString("\",\"", prefix = "\"", postfix = "\"") { TagFactory.generateTag(it) }
        } else null

        val configurationsTag = if (application.configurations?.isNotEmpty() == true) {
            application.configurations.flatMap { it.modules }.joinToString("\",\"", prefix = "\"", postfix = "\"") { TagFactory.generateTag(it) }
        } else null

        val includesString = includesTags?.let { ", includes=[$it]" } ?: ""
        val configurationsString = configurationsTag?.let { ", configurations=[$it]" } ?: ""

        return """
            @$metaApplication("$fullpath"$includesString$configurationsString)
        """.trimIndent()
    }

    fun generate(module: KoinMetaData.Module): String {
        val fullpath = module.fullpath
        val moduleId = module.hashId

        val includesTags = if (module.includes?.isNotEmpty() == true) {
            module.includes.joinToString("\",\"", prefix = "\"", postfix = "\"") { TagFactory.generateTag(it) }
        } else null

        val configurationsTag = if (module.configurationTags?.isNotEmpty() == true) {
            module.configurationTags.joinToString("\",\"", prefix = "\"", postfix = "\""){ it.name }
        } else null

        val includesString = includesTags?.let { ", includes=[$it]" } ?: ""
        val configurationsString = configurationsTag?.let { ", configurations=[$it]" } ?: ""

        return """
            @$metaModule("$fullpath",id="$moduleId"$includesString$configurationsString)
        """.trimIndent()
    }

    fun generate(def: KoinMetaData.Definition, module : KoinMetaData.Module): String {
        val fullpath = def.packageName + "." + def.label
        val dependencies = def.parameters.filterIsInstance<KoinMetaData.DefinitionParameter.Dependency>()

        val bindings = def.bindings.map { it.qualifiedName?.asString() to it }.filter { (v,_) -> v !in fullWhiteList }.map { (v, t) -> TagFactory.generateTag( v,t) }
        val boundTypes = if (bindings.isNotEmpty()) bindings.joinToString(
            "\",\"",
            prefix = "\"",
            postfix = "\""
        ) else null

        val qualifier = def.qualifier

        val cleanedDependencies = dependencies
            .filter { !it.alreadyProvided && !it.hasDefault && !it.isNullable }
            .mapNotNull { dep ->
                if (dep.kind == DependencyKind.SingleValue) TagFactory.generateTag(dep)
                else {
                    val ksDeclaration = extractLazyOrListType(dep)
                    ksDeclaration?.let { TagFactory.generateTag(def, dep ,ksDeclaration) }
                }
            }

        val depsTags = if (cleanedDependencies.isNotEmpty()) cleanedDependencies.joinToString(
            "\",\"",
            prefix = "\"",
            postfix = "\""
        ) else null

        val depsString = depsTags?.let { ", dependencies=[$it]" } ?: ""

        val scopeDef = if (def.isScoped()) def.scope?.getValue() else null
        val scopeString = scopeDef?.let { ", scope=\"$it\"" } ?: ""
        val bindsString = boundTypes?.let { ", binds=[$it]" } ?: ""
        val qualifierString = qualifier?.let { ", qualifier=\"$it\"" } ?: ""

        val tagId = "${module.hashId}:${TagFactory.generateTag(module)}"

        return """
            @$metaDefinition("$fullpath",moduleTagId="$tagId"$depsString$scopeString$bindsString$qualifierString)
        """.trimIndent()
    }

    private fun extractLazyOrListType(it: KoinMetaData.DefinitionParameter.Dependency) =
        it.type.arguments.first().type?.resolve()?.declaration
}