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

val MODULE_FOOTER = "}"

val DEFAULT_MODULE_FOOTER = """
    }
    public val defaultModule : org.koin.core.module.Module get() = _defaultModule
    public fun org.koin.core.KoinApplication.defaultModule(): org.koin.core.KoinApplication = modules(defaultModule)
""".trimIndent()

val MODULE_HEADER = """
    package org.koin.ksp.generated
    
    import org.koin.core.module.Module
    import org.koin.dsl.*
""".trimIndent()
