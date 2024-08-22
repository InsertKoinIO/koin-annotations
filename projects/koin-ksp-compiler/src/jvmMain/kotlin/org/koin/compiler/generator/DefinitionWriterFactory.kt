/*
 * Copyright 2017-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.koin.compiler.generator

import com.google.devtools.ksp.processing.Resolver
import org.koin.compiler.generator.ModuleWriter.Companion.MODULE_INSTANCE
import org.koin.compiler.metadata.KoinMetaData
import java.io.OutputStream

class DefinitionWriterFactory(
    resolver: Resolver,
    fileStream: OutputStream
) {
    private val writer = DefinitionWriter(resolver, fileStream)

    fun writeDefinition(
        definition : KoinMetaData.Definition,
        module: KoinMetaData.Module? = null,
        isExternal : Boolean? = null
    ) {
        return when (definition) {
            is KoinMetaData.Definition.FunctionDefinition -> {
                if (definition.isClassFunction) {
                    if (module?.type?.isObject == true) {
                        val modulePath = "${module.packageName}.${module.name}"
                        // Object Class function
                        writer.writeDefinition(definition, prefix = "$modulePath.${definition.functionName}", isExternalDefinition = isExternal ?: false)
                    } else {
                        // Module function
                        writer.writeDefinition(definition, prefix = "$MODULE_INSTANCE.${definition.functionName}")
                    }
                } else {
                    // Pure Function
                    writer.writeDefinition(definition, prefix = "${definition.packageNamePrefix}${definition.functionName}", isExternalDefinition = isExternal ?: false)
                }
            }
            // Class
            is KoinMetaData.Definition.ClassDefinition -> writer.writeDefinition(definition, prefix = "${definition.packageNamePrefix}${definition.className}", isExternalDefinition = isExternal ?: false)
        }
    }

}