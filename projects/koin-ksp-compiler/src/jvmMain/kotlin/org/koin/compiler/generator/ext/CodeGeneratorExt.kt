package org.koin.compiler.generator.ext

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.OutputStream

fun CodeGenerator.getNewFile(packageName: String = "org.koin.ksp.generated", fileName: String): OutputStream {
    return try {
        createNewFile(
            Dependencies.ALL_FILES,
            packageName,
            fileName
        )
    } catch (ex: FileAlreadyExistsException){
        ex.file.outputStream()
    }
}