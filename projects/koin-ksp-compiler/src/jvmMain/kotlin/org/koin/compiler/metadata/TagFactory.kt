package org.koin.compiler.metadata

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.scanner.ext.filterForbiddenKeywords
import org.koin.compiler.scanner.ext.getPackageName
import org.koin.compiler.type.clearPackageSymbols
import org.koin.compiler.verify.DefinitionVerification
import org.koin.compiler.verify.qualifiedNameCamelCase

const val KOIN_TAG_SEPARATOR = "_"
private const val QUALIFIER_SYMBOL = "Q_"
private const val SCOPE_SYMBOL = "S_"

object TagFactory {

    private fun getTag(module: KoinMetaData.Module): String {
        return with(module) {
            listOfNotNull(
                packageName.clearPackageSymbols()+"." + name,
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun getTagClass(module: KoinMetaData.Module): String {
        return getTag(module).camelCase()
    }

    fun getMetaTag(module: KoinMetaData.ModuleInclude): String {
        return with(module) {
            listOfNotNull(
                packageName.clearPackageSymbols() + "." +className.capitalize(),
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun getTagClass(definition: KoinMetaData.Definition, clazz: KSDeclaration): String {
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

    fun getMetaTag(definition: KoinMetaData.Definition,dep: KoinMetaData.DefinitionParameter.Dependency ,clazz: KSDeclaration): String {
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

    fun updateTagWithScope(classTag: String, dv: DefinitionVerification) =  "$classTag$KOIN_TAG_SEPARATOR$SCOPE_SYMBOL${dv.scope}"
    fun getTagClass(clazz: KSDeclaration): String = clazz.qualifiedNameCamelCase() ?: ""

    fun getTagClass(definition: KoinMetaData.Definition): String {
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

    fun getMetaTag(dep: KoinMetaData.DefinitionParameter.Dependency): String {
        return with(dep) {
            val ksClassDeclaration = (dep.type.declaration as KSClassDeclaration)
            val packageName = ksClassDeclaration.getPackageName().filterForbiddenKeywords()
            val className = ksClassDeclaration.simpleName.asString()
            val isExpect = ksClassDeclaration.isExpect
            val isActual = ksClassDeclaration.isActual

            val packageNameConsolidated = packageName + "." + className.capitalize()
            listOfNotNull(
                packageNameConsolidated,
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