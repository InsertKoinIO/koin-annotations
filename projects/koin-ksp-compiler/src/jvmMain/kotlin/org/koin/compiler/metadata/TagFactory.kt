package org.koin.compiler.metadata

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.scanner.ext.filterForbiddenKeywords
import org.koin.compiler.scanner.ext.getPackageName
import org.koin.compiler.verify.qualifiedNameCamelCase
import java.util.*

const val KOIN_TAG_SEPARATOR = "_"

object TagFactory {

    fun getTag(module: KoinMetaData.Module): String {
        return with(module) {
            listOfNotNull(
                packageName.camelCase() + name,
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun getTag(module: KoinMetaData.ModuleInclude): String {
        return with(module) {
            listOfNotNull(
                packageName.camelCase() + className.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun getTag(definition: KoinMetaData.Definition, clazz: KSDeclaration): String {
        return with(definition) {
            listOfNotNull(
                clazz.qualifiedNameCamelCase()?.clearPackageSymbols() ?: "",
                qualifier?.let { "Q_${escapeTagClass(it)}" },
                scope?.getTagValue()?.camelCase()?.let { "S_$it" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun getTag(clazz: KSDeclaration): String {
        return clazz.qualifiedNameCamelCase() ?: ""
    }

    fun getTag(definition: KoinMetaData.Definition): String {
        return with(definition) {
            listOfNotNull(
                packageName.camelCase() + label.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                qualifier?.let { "Q_${escapeTagClass(it)}" },
                scope?.getTagValue()?.camelCase()?.let { "S_$it" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun getTag(dep: KoinMetaData.DefinitionParameter.Dependency): String {
        return with(dep) {
            val ksClassDeclaration = (dep.type.declaration as KSClassDeclaration)
            val packageName = ksClassDeclaration.getPackageName().filterForbiddenKeywords()
            val className = ksClassDeclaration.simpleName.asString()
            val isExpect = ksClassDeclaration.isExpect
            val isActual = ksClassDeclaration.isActual

            val packageNameConsolidated =
                packageName.clearPackageSymbols().camelCase() + className.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            listOfNotNull(
                packageNameConsolidated,
                qualifier?.let { "Q_${escapeTagClass(it)}" },
//                scopeId?.let { "S_$it" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    internal fun String.clearPackageSymbols() = replace("`","").replace("'","")

    private fun escapeTagClass(qualifier : String) : String {
        return if (!qualifier.contains(".")) qualifier
        else {
            qualifier.split(".").joinToString("") { it.capitalize() }
        }
    }

    fun getTagFromFullPath(path: String) : String {
        return path.split(".").joinToString(separator = "") { it.capitalize() }
    }
}