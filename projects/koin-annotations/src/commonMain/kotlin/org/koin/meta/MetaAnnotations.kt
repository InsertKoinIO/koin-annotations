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
package org.koin.meta

/**
 * All following Annotations are intended for Internal use only
 *
 * @author Arnaud Giuliani
 */

/**
 * Meta Definition annotation to help represents
 * - Definition full path
 * - Parameters Tags to check
 */
@Target(AnnotationTarget.CLASS)
annotation class MetaDefinition(val value: String = "",val parameters : Array<String> = [])

/**
 * Meta Definition annotation to help represents
 * - Definition full path
 * - Includes Module Tags to check
 */
@Target(AnnotationTarget.CLASS)
annotation class MetaModule(val value: String = "",val includes : Array<String> = [])
