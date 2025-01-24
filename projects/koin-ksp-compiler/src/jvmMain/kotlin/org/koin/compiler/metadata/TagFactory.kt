package org.koin.compiler.metadata

import com.google.devtools.ksp.symbol.KSDeclaration
import org.koin.compiler.verify.qualifiedNameCamelCase
import java.util.*

const val KOIN_TAG_SEPARATOR = ""

object TagFactory {

    fun getTag(module: KoinMetaData.Module): String {
        return with(module) {
            listOfNotNull(
                packageName.camelCase(),
                name,
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }

    fun getTag(module: KoinMetaData.ModuleInclude): String {
        return with(module) {
            listOfNotNull(
                packageName.camelCase(),
                className.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
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
                packageName.camelCase(),
                label.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                qualifier?.let { "Q_$it" },
                scope?.getTagValue()?.camelCase()?.let { "S_$it" },
                if (isExpect) "Expect" else null,
                if (isActual) "Actual" else null
            ).joinToString(separator = KOIN_TAG_SEPARATOR)
        }
    }
}