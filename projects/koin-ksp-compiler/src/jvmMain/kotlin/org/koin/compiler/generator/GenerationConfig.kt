package org.koin.compiler.generator

import org.koin.compiler.generator.KoinCodeGenerator.Companion.LOGGER

object GenerationConfig {

    private const val DEFAULT_GEN_PACKAGE = "org.koin.ksp.generated"
    private var generationPath : String = DEFAULT_GEN_PACKAGE

    fun setGenerationPath(path : String?){
        if (path != null){
            if (isValidKotlinPackageName(path)){
                generationPath = path
                LOGGER.warn("Koin generation path: '$generationPath'")
            } else {
                throw IllegalArgumentException("Invalid Kotlin package name: '$path'")
            }
        }
    }

    private fun isValidKotlinPackageName(packageName: String): Boolean {
        if (packageName.isEmpty()) return false
        
        val segments = packageName.split('.')
        return segments.all { segment ->
            segment.isNotEmpty() && 
            isValidKotlinIdentifier(segment) &&
            !isKotlinKeyword(segment)
        }
    }

    fun isValidKotlinIdentifier(identifier: String): Boolean {
        if (identifier.isEmpty()) return false
        
        val first = identifier.first()
        if (!first.isLetter() && first != '_') return false
        
        return identifier.all { it.isLetterOrDigit() || it == '_' }
    }

    fun isKotlinKeyword(word: String): Boolean {
        val keywords = setOf(
            // Hard keywords
            "as", "break", "class", "continue", "do", "else", "false", "for",
            "fun", "if", "in", "interface", "is", "null", "object", "package",
            "return", "super", "this", "throw", "true", "try", "typealias",
            "typeof", "val", "var", "when", "while",
            // Soft keywords that should be avoided in package names
            "by", "catch", "constructor", "delegate", "dynamic", "field", "file",
            "finally", "get", "import", "init", "param", "property", "receiver",
            "set", "setparam", "where"
        )
        return word in keywords
    }

    fun getGenerationPath() = generationPath
}