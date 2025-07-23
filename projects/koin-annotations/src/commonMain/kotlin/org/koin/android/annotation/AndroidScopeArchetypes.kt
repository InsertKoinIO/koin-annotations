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
package org.koin.android.annotation

/**
 * Declare a class in a ViewModelScope Koin scope.
 *
 * example:
 *
 * @ActivityModelScope
 * class MyClass(val d : MyDependency)
 *
 * will generate:
 * ```
 * activityScope {
 *  scoped { MyClass(get()) }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ActivityScope

/**
 * Declare a class in a ViewModelScope Koin scope.
 *
 * example:
 *
 * @ActivityRetainedModelScope
 * class MyClass(val d : MyDependency)
 *
 * will generate:
 * ```
 * activityRetainedScope {
 *  scoped { MyClass(get()) }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class ActivityRetainedScope

/**
 * Declare a class in a ViewModelScope Koin scope.
 *
 * example:
 *
 * @FragmentModelScope
 * class MyClass(val d : MyDependency)
 *
 * will generate:
 * ```
 * fragmentScope {
 *  scoped { MyClass(get()) }
 * }
 * ```
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class FragmentScope