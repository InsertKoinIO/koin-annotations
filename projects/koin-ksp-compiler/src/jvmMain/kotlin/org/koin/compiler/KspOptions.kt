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
package org.koin.compiler

/**
 * Koin Compiler Options
 *
 * KOIN_CONFIG_CHECK - Boolean - check all Koin configuration at compile time (Koin compile safety)
 * KOIN_LOG_TIMES - Boolean - display logs for module generation time
 * KOIN_DEFAULT_MODULE - Boolean - generate a default module if no module is found for a given definition
 * KOIN_GENERATION_PACKAGE - String - package to generate generated Koin classes
 * KOIN_USE_COMPOSE_VIEWMODEL - Boolean - generate viewModel using koin-core-viewmodel (Multiplatform compatible)
 */
enum class KspOptions {
    KOIN_CONFIG_CHECK,
    KOIN_LOG_TIMES,
    KOIN_DEFAULT_MODULE,
    KOIN_GENERATION_PACKAGE,
    KOIN_USE_COMPOSE_VIEWMODEL,
    KOIN_EXPORT_DEFINITIONS
}