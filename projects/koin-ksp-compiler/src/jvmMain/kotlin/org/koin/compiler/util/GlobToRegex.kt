package org.koin.compiler.util


/**
 * Converts this string, interpreted as a glob pattern, to a regular expression.
 *
 * @param ignoreCase if true, the resulting regex will be case-insensitive.
 *                  Default is false, as package names are typically case-sensitive.
 * @return a [Regex] object that matches strings according to this glob pattern.
 *
 * @throws IllegalArgumentException if the glob pattern contains `**` without a preceding dot.
 *
 * @author OffRange
 */
fun String.toGlobRegex(ignoreCase: Boolean = false): Regex {
    // 1) Escape dots
    //    so we can safely do replacements for * and ** below.
    val escaped = replace(".", "\\.")

    // 2) Replace '**' with a unique placeholder (so we don't conflict with single '*')
    val doubleStarPlaceholder = "\u0000"
    val afterDoubleStar = escaped.replace("**", doubleStarPlaceholder)

    // 3) Replace single '*' with [^.]* (zero-or-more non-dot characters)
    val afterSingleStar = afterDoubleStar.replace("*", "[^.]*")

    // 4) Replace our placeholder with .* (zero-or-more of any character)
    val afterPlaceholder = afterSingleStar.replace(doubleStarPlaceholder, ".*")

    // 5) Wrap with ^ and $ for a full-match regex
    return with("^${afterPlaceholder.ensureGlobPattern()}$") {
        if (ignoreCase) toRegex(RegexOption.IGNORE_CASE) else toRegex()
    }
}

/**
 * Checks if this map contains a key that matches the given glob pattern.
 *
 * @param keyGlob the glob pattern to match against keys.
 * @return true if any key matches the glob pattern, false otherwise.
 *
 * @author OffRange
 */
fun Map<String, *>.anyMatch(keyGlob: String): Boolean = keys.any { keyGlob.toGlobRegex().matches(it) }

/**
 * Checks if this string matches the given glob pattern.
 *
 * @param glob the glob pattern to match.
 * @param ignoreCase if true, the match will be case-insensitive.
 *                  Default is false, as package and class names are typically case-sensitive.
 * @return true if this string matches the glob pattern, false otherwise.
 *
 * @author OffRange
 */
fun String.matchesGlob(glob: String, ignoreCase: Boolean = false): Boolean =
    glob.toGlobRegex(ignoreCase = ignoreCase).matches(this)

/**
 * Ensures that this string is a valid glob pattern.
 * Implemented to ensure backwards compatibility, as `com.example` would ONLY match this package.
 *
 * @receiver the string to ensure as a glob pattern.
 * @return this string if it already contains a glob pattern, otherwise this string with a recursive glob pattern appended.
 *
 * @author OffRange
 */
private fun String.ensureGlobPattern(): String = if (contains('*')) this else "$this.*"