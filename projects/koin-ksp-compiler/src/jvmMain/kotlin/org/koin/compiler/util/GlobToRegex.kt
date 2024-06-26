package org.koin.compiler.util

/**
 * A utility object for converting glob patterns to regular expressions, primarily used for matching package names.
 *
 * Glob patterns are a simple way to match file paths and, in this context, package names.
 * They use wildcards to represent variable parts of the name:
 *
 * - `*`: Matches any sequence of characters within a single package level.
 *        Example: `com.example.*` matches `com.example.foo` but not `com.example.foo.bar`.
 *
 * - `**`: Matches any sequence of characters across multiple package levels.
 *         Example: `com.example.**` matches both `com.example.foo` and `com.example.foo.bar`.
 *
 * @author OffRange
 * @see [convert]
 * @see [String.toGlobRegex]
 * @see [String.matchesGlob]
 */
object GlobToRegex {
    private const val DOT = '.'
    private const val ESCAPED_DOT = "\\$DOT"
    private const val NOT_DOT = "[^$DOT]"

    private const val MULTI_LEVEL_WILDCARD = "**"
    private const val SINGLE_LEVEL_WILDCARD = "*"

    private const val GENERAL_SINGLE_LEVEL_PATTERN = "$NOT_DOT*"
    private const val GENERAL_MULTI_LEVEL_PATTERN = "($GENERAL_SINGLE_LEVEL_PATTERN$ESCAPED_DOT)*$NOT_DOT+"

    /**
     * Converts a glob pattern to a regular expression.
     *
     * Supports two types of wildcards:
     * - `*`: Matches any characters within a single package level.
     * - `**`: Matches any characters across multiple package levels.
     *
     * @param globPattern the glob pattern to convert, e.g., "com.example.**.service.*"
     * @param ignoreCase if true, the resulting regex will be case-insensitive. Default is false,
     *                  as package names in most JVM languages are case-sensitive.
     * @return a [Regex] object that matches strings according to the given glob pattern.
     *
     * @throws IllegalArgumentException if the glob pattern is invalid or cannot be converted.
     */
    fun convert(globPattern: String, ignoreCase: Boolean = false): Regex {
        val parts = globPattern.split(DOT)
        val regexParts = parts.map { part ->
            when (part) {
                MULTI_LEVEL_WILDCARD -> GENERAL_MULTI_LEVEL_PATTERN
                SINGLE_LEVEL_WILDCARD -> GENERAL_SINGLE_LEVEL_PATTERN
                else -> part.replace(
                    SINGLE_LEVEL_WILDCARD,
                    GENERAL_SINGLE_LEVEL_PATTERN
                )
            }
        }
        return with("^${regexParts.joinToString(ESCAPED_DOT)}$") {
            if (ignoreCase) toRegex(RegexOption.IGNORE_CASE) else toRegex()
        }
    }
}

/**
 * Converts this string, interpreted as a glob pattern, to a regular expression.
 *
 * @param ignoreCase if true, the resulting regex will be case-insensitive.
 *                  Default is false, as package names are typically case-sensitive.
 * @return a [Regex] object that matches strings according to this glob pattern.
 */
fun String.toGlobRegex(ignoreCase: Boolean = false): Regex = GlobToRegex.convert(this, ignoreCase)

/**
 * Checks if this map contains a key that matches the given glob pattern.
 *
 * @param keyGlob the glob pattern to match against keys.
 * @return true if any key matches the glob pattern, false otherwise.
 */
fun Map<String, *>.anyMatch(keyGlob: String): Boolean = keys.any { keyGlob.toGlobRegex().matches(it) }

/**
 * Checks if this string matches the given glob pattern.
 *
 * @param glob the glob pattern to match.
 * @param ignoreCase if true, the match will be case-insensitive.
 *                  Default is false, as package and class names are typically case-sensitive.
 * @return true if this string matches the glob pattern, false otherwise.
 */
fun String.matchesGlob(glob: String, ignoreCase: Boolean = false): Boolean =
    glob.toGlobRegex(ignoreCase = ignoreCase).matches(this)