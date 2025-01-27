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
package org.koin.meta.annotations

/**
 * All following Annotations are intended for Internal use only
 *
 * @author Arnaud Giuliani
 */

/**
 * Internal usage for components discovery in generated package
 *
 * @param value: package of declared definition
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class ExternalDefinition(val value: String = "")

/**
 * Meta Definition annotation to help represents
 * @param value: Definition full path
 * @param dependencies - Parameters Tags to check
 * @param scope - Scope where it's declared
 */
@Target(AnnotationTarget.CLASS)
annotation class MetaDefinition(val value: String = "", val dependencies: Array<String> = [], val scope : String = "")

/**
 * Meta Definition annotation to help represents
 * @param value: Definition full path
 * @param includes - Includes Module Tags to check
 */
@Target(AnnotationTarget.CLASS)
annotation class MetaModule(val value: String = "", val includes: Array<String> = [])
