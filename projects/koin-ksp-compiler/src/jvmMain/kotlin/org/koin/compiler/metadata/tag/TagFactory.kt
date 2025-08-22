package org.koin.compiler.metadata.tag

import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.camelCase
import org.koin.compiler.type.clearPackageSymbols
import org.koin.compiler.verify.MetaDefinitionData
import org.koin.compiler.verify.qualifiedNameCamelCase

const val KOIN_TAG_SEPARATOR = "_"
internal const val QUALIFIER_SYMBOL = "Q_"
internal const val SCOPE_SYMBOL = "S_"

object TagFactory {

    fun generateTag(app: KoinMetaData.Application): String {
        return with(app) {
            listOfNotNull(
                packageName.clearPackageSymbols()+"." + name,
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }.camelCase()
    }

    fun generateTag(module: KoinMetaData.Module): String {
        return with(module) {
            listOfNotNull(
                packageName.clearPackageSymbols()+"." + name,
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }.camelCase()
    }

    fun generateTag(module: KoinMetaData.ModuleInclude): String {
        return with(module) {
            listOfNotNull(
                packageName.clearPackageSymbols() + "." +className.capitalize(),
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun generateTag(definition: KoinMetaData.Definition, clazz: KSDeclaration): String {
        return with(definition) {
            listOfNotNull(
                clazz.qualifiedName?.asString() ?: "",
                qualifier?.let { "$QUALIFIER_SYMBOL${escapeTagClass(it)}" },
                scope?.getTagValue()?.camelCase()?.let { "$SCOPE_SYMBOL$it" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR).camelCase()
        }
    }

    fun generateTag(definition: KoinMetaData.Definition, dep: KoinMetaData.DefinitionParameter.Dependency, clazz: KSDeclaration): String {
        return with(definition) {
            listOfNotNull(
                clazz.qualifiedName?.asString() ?: "",
                qualifier?.let { "$QUALIFIER_SYMBOL${escapeTagClass(it)}" },
                scope?.getTagValue()?.camelCase()?.let { "$SCOPE_SYMBOL$it" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(prefix = "${dep.name}:", separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun generateTag(name : String?, binding: KSDeclaration): String {
        return listOfNotNull(
            name,
            if (binding.isExpect) "Expect" else null,
            if (binding.isActual) "Actual" else null
        ).joinToString(separator = KOIN_TAG_SEPARATOR)
    }

    fun updateTagWithScope(classTag: String, dv: MetaDefinitionData) =  "$classTag$KOIN_TAG_SEPARATOR$SCOPE_SYMBOL${dv.scope}"
    fun generateTag(clazz: KSDeclaration): String = clazz.qualifiedNameCamelCase() ?: ""

    fun generateTag(definition: KoinMetaData.Definition): String {
        return with(definition) {
            listOfNotNull(
                packageName.camelCase().clearPackageSymbols() + label.capitalize(),
                qualifier?.let { "$QUALIFIER_SYMBOL${escapeTagClass(it)}" },
                scope?.getTagValue()?.camelCase()?.let { "$SCOPE_SYMBOL$it" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun generateTag(dep: KoinMetaData.DefinitionParameter.Dependency): String {
        return with(dep) {
            val ksDeclaration = dep.type.declaration
            val fullClassName = ksDeclaration.qualifiedName?.asString()
            val isExpect = ksDeclaration.isExpect
            val isActual = ksDeclaration.isActual

            listOfNotNull(
                fullClassName,
                qualifier?.let { "$QUALIFIER_SYMBOL${escapeTagClass(it)}" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(prefix = "${dep.name}:", separator = KOIN_TAG_SEPARATOR)
        }
    }

    private fun escapeTagClass(qualifier : String) : String {
        return if (!qualifier.contains(".")) qualifier
        else {
            qualifier.split(".").joinToString("") { it.capitalize() }
        }
    }
}