package org.koin.compiler.metadata

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.scanner.ext.filterForbiddenKeywords
import org.koin.compiler.scanner.ext.getPackageName
import org.koin.compiler.verify.DefinitionVerification
import org.koin.compiler.verify.qualifiedNameCamelCase
import java.util.*

const val KOIN_TAG_SEPARATOR = "_"
private const val QUALIFIER_SYMBOL = "Q_"
private const val SCOPE_SYMBOL = "S_"

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
                qualifier?.let { "$QUALIFIER_SYMBOL${escapeTagClass(it)}" },
                scope?.getTagValue()?.camelCase()?.let { "$SCOPE_SYMBOL$it" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun getTag(classTag: String, dv: DefinitionVerification) =  "$classTag$KOIN_TAG_SEPARATOR$SCOPE_SYMBOL${dv.scope}"

    fun getTag(clazz: KSDeclaration): String = clazz.qualifiedNameCamelCase() ?: ""

    fun getTag(definition: KoinMetaData.Definition): String {
        return with(definition) {
            listOfNotNull(
                packageName.camelCase() + label.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                qualifier?.let { "$QUALIFIER_SYMBOL${escapeTagClass(it)}" },
                scope?.getTagValue()?.camelCase()?.let { "$SCOPE_SYMBOL$it" },
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
                qualifier?.let { "$QUALIFIER_SYMBOL${escapeTagClass(it)}" },
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