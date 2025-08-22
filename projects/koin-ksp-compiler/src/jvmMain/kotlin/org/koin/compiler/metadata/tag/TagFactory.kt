package org.koin.compiler.metadata.tag

import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.metadata.KoinMetaData
import org.koin.compiler.metadata.camelCase
import org.koin.compiler.type.clearPackageSymbols
import org.koin.compiler.verify.MetaDefinitionData
import org.koin.compiler.verify.qualifiedNameCamelCase

object TagFactory {
    private const val KOIN_TAG_SEPARATOR = "_"
    private const val QUALIFIER_SYMBOL = "Q_"
    private const val SCOPE_SYMBOL = "S_"
    private const val TAG_PREFIX = "_KSP_"
    const val DEFAULT_GEN_PACKAGE = "org.koin.ksp.generated"

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

    fun generateTag(scope : KoinMetaData.Scope.ClassScope) : String{
        return scope.type.qualifiedNameCamelCase() ?: ""
    }

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

    fun createTagForScope(classTag: String, def: MetaDefinitionData) = "$classTag$KOIN_TAG_SEPARATOR$SCOPE_SYMBOL${def.scope}"
    fun createTagForQualifier(classTag: String, def: MetaDefinitionData) = "$classTag$KOIN_TAG_SEPARATOR$QUALIFIER_SYMBOL${def.qualifier}"
    fun prefixTag(classTag: String, withGenPackage : Boolean) = if (withGenPackage) "$DEFAULT_GEN_PACKAGE.$TAG_PREFIX$classTag" else "$TAG_PREFIX$classTag"

    private fun escapeTagClass(qualifier : String) : String {
        return if (!qualifier.contains(".")) qualifier
        else {
            qualifier.split(".").joinToString("") { it.capitalize() }
        }
    }
}