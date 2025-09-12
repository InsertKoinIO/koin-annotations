package org.koin.compiler.generator

import com.google.devtools.ksp.processing.CodeGenerator
import org.koin.compiler.generator.ext.appendText
import org.koin.compiler.generator.ext.getNewFile
import org.koin.compiler.metadata.KoinMetaData
import java.io.OutputStream

abstract class AbstractFileWriter(val codeGenerator: CodeGenerator) {

    abstract val fileName: String
    protected fun createFileStream(): OutputStream = codeGenerator.getNewFile(fileName = fileName)
    protected var fileStream: OutputStream? = null
    protected fun write(string: String) { fileStream?.appendText(string) }
    protected fun writeln(line: String) = write("$line\n")
    protected fun writeEmptyLine() = writeln("")

    protected fun generateIncludes(modules : List<KoinMetaData.ModuleInclude>): String {
        return modules.joinToString(separator = ",\n\t\t") {
            val ctor = if (!it.isObject) "()" else ""
            if (it.packageName.isEmpty()) "${it.className}$ctor.module"
            else "${it.packageName}.${it.className}$ctor.module"
        }
    }
}