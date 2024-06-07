package org.koin.compiler.util

object GlobToRegex {
    private const val DOT = '.'
    private const val ESCAPED_DOT = "\\$DOT"
    private const val NOT_DOT = "[^$DOT]+"

    private const val MULTI_LEVEL_WILDCARD = "**"
    private const val SINGLE_LEVEL_WILDCARD = "*"

    private const val SINGLE_LEVEL_PATTERN = "$NOT_DOT$ESCAPED_DOT?"
    private const val MULTI_LEVEL_PATTERN = "($SINGLE_LEVEL_PATTERN)*"

    fun convert(globPattern: String, ignoreCase: Boolean = false): Regex {
        val parts = globPattern.split(DOT)
        val regexParts = parts.mapIndexed { index, part ->
            when {
                part == MULTI_LEVEL_WILDCARD -> multiLevelPatternFor(index == parts.lastIndex)
                part.contains(SINGLE_LEVEL_WILDCARD) -> part.replace(SINGLE_LEVEL_WILDCARD, SINGLE_LEVEL_PATTERN)
                else -> Regex.escape(part) + if (index < parts.lastIndex) ESCAPED_DOT else ""
            }
        }
        return with("^${regexParts.joinToString("")}$") {
            if (ignoreCase) toRegex(RegexOption.IGNORE_CASE) else toRegex()
        }
    }

    private fun multiLevelPatternFor(isLast: Boolean) =
        if (isLast) "$MULTI_LEVEL_PATTERN$NOT_DOT" else MULTI_LEVEL_PATTERN
}

fun String.toGlobRegex(ignoreCase: Boolean = false): Regex = GlobToRegex.convert(this, ignoreCase)

fun Map<String, *>.containsGlob(keyGlob: String): Boolean = keys.any { keyGlob.toGlobRegex().matches(it) }

fun String.containsGlob(glob: String, ignoreCase: Boolean = false): Boolean =
    glob.toGlobRegex(ignoreCase = ignoreCase).matches(this)