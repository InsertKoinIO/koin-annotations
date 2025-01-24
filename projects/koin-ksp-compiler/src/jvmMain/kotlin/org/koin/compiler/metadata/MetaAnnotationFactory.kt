package org.koin.compiler.metadata

import org.koin.meta.annotations.MetaModule

object MetaAnnotationFactory {

    fun generate(module: KoinMetaData.Module): String {
        val annotation = MetaModule::class.simpleName!!
        val fullpath = module.packageName + "." + module.name
        val includesTags = if (module.includes?.isNotEmpty() == true) {
            module.includes.joinToString(",", prefix = "\"", postfix = "\"") { TagFactory.getTag(it) }
        } else null
        val includesString = includesTags?.let { ", includes=[$it]" } ?: ""
        return """
            @$annotation("$fullpath"$includesString)
        """.trimIndent()
    }
}