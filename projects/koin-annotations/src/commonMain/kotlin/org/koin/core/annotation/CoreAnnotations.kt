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
package org.koin.core.annotation

import kotlin.reflect.KClass

/**
 * Koin Annotations
 *
 * @author Arnaud Giuliani
 */

/**
 * Koin definition annotation
 * Declare a type, a function as `single` definition in Koin
 *
 * example:
 *
 * @Single
 * class MyClass(val d : MyDependency)
 *
 * will result in `single { MyClass(get()) }`
 *
 * All dependencies are filled by constructor.
 *
 * @param binds: declared explicit types to bind to this definition. Supertypes are automatically detected
 * @param createdAtStart: create instance at Koin start
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Single(val binds: Array<KClass<*>> = [Unit::class], val createdAtStart: Boolean = false)

/**
 * same as @Single
 * @see Single
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Singleton(val binds: Array<KClass<*>> = [Unit::class], val createdAtStart: Boolean = false)

/**
 * Koin definition annotation
 * Declare a type, a function as `factory` definition in Koin
 *
 * example:
 *
 * @Factory
 * class MyClass(val d : MyDependency)
 *
 * will result in `factory { MyClass(get()) }`
 *
 * All dependencies are filled by constructor.
 *
 * @param binds: declared explicit types to bind to this definition. Supertypes are automatically detected
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Factory(val binds: Array<KClass<*>> = [Unit::class])

/**
 * Declare a class in a Koin scope. Scope name is described by either value (class) or name (string)
 * By default, will declare a `scoped` definition. Can also override with @Scoped, @Factory, @KoinViewModel annotations to add explicit bindings
 *
 * example:
 *
 * @Scope(MyScope::class)
 * class MyClass(val d : MyDependency)
 *
 * will generate:
 * ```
 * scope<MyScope> {
 *  scoped { MyClass(get()) }
 * }
 * ```
 *
 * @param value: scope class value
 * @param name: scope string value
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Scope(val value: KClass<*> = Unit::class, val name: String = "")

/**
 * Declare a type, a function as `scoped` definition in Koin. Must be associated with @Scope annotation
 * @see Scope
 *
 * @param binds: declared explicit types to bind to this definition. Supertypes are automatically detected
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Scoped(val binds: Array<KClass<*>> = [Unit::class])

/**
 * Annotate a parameter from class constructor or function, to ask resolution for given scope with Scope Id
 *
 * ScopedId can be defined with a String (name parameter) or a type (value parameter)
 *
 * example:
 *
 * @Factory
 * class MyClass(@ScopeId(name = "my_scope_id") val d : MyDependency)
 *
 * will result in `factory { MyClass(getScope("my_scope_id").get()) }`
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ScopeId(val value: KClass<*> = Unit::class, val name: String = "")

/**
 * Define a qualifier for a given definition (associated with Koin definition annotation)
 * Will generate `StringQualifier("...")`
 *
 * @param value: string qualifier
 * @param type: class qualifier
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class Named(val value: String = "", val type: KClass<*> = Unit::class)

/**
 * Define a qualifier for a given definition (associated with Koin definition annotation)
 * Will generate `StringQualifier("...")`
 *
 * @param value: class qualifier
 * @param name: string qualifier
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class Qualifier(val value: KClass<*> = Unit::class, val name: String = "")

/**
 * Annotate a constructor parameter or function parameter, to ask resolution as "injected parameter"
 *
 * example:
 *
 * @Factory
 * class MyClass(@InjectedParam val d : MyDependency)
 *
 * will result in `factory { params -> MyClass(params.get()) }`
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class InjectedParam

/**
 * Annotate a constructor parameter or function parameter, to resolve as Koin property
 *
 * example:
 *
 * @Factory
 * class MyClass(@Property("name") val name : String)
 *
 * will result in `factory { MyClass(getProperty("name")) }`
 *
 * @param value: property name
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Property(val value: String)

/**
 * Annotate a field value that will be Property default value
 *
 * @PropertyValue("name")
 * val defaultName = "MyName"
 *
 * @Factory
 * class MyClass(@Property("name") val name : String)
 *
 * will result in `factory { MyClass(getProperty("name", defaultName)) }`
 */
@Target(AnnotationTarget.FIELD)
annotation class PropertyValue(val value: String)

/**
 * Class annotation, to help gather definitions inside a Koin module.
 * Each function can be annotated with a Koin definition annotation, to declare it
 *
 * example:
 *
 * @Module
 * class MyModule {
 *  @Single
 *  fun myClass(d : MyDependency) = MyClass(d)
 * }
 *
 * will generate:
 *
 * ```
 * val MyModule.module = module {
 *  val moduleInstance = MyModule()
 *  single { moduleInstance.myClass(get()) }
 * }
 *
 *
 * @param includes: Module Classes to include
 */
@Target(AnnotationTarget.CLASS)
annotation class Module(val includes: Array<KClass<*>> = [], val createdAtStart: Boolean = false)

/**
 * Gather definitions declared with Koin definition annotation
 * Will scan in current package or with the explicit package name
 *
 * The [value] parameter supports both exact package names and glob patterns:
 *
 * 1. Exact package: `"com.example.service"`
 *    - Scans only the `com.example.service` package.
 *
 * 2. Single-level wildcard (`*`): `"com.example.*.service"`
 *    - Matches one level of package hierarchy.
 *    - E.g., `com.example.user.service`, `com.example.order.service`.
 *    - Does NOT match `com.example.service` or `com.example.user.impl.service`.
 *
 * 3. Multi-level wildcard (`**`): `"com.example.**"`
 *    - Matches any number of package levels.
 *    - E.g., `com.example`, `com.example.service`, `com.example.service.user`.
 *
 * Wildcards can be combined and used at any level:
 * - `"com.**.service.*data"`: All packages that ends with "data" in any `service` subpackage.
 * - `"com.*.service.**"`: All classes in `com.X.service` and its subpackages.
 *
 * @param value The package to scan. Can be an exact package name or a glob pattern.
 *              Defaults to the package of the annotated element if empty.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
annotation class ComponentScan(val value: String = "")

/**
 * Tag a dependency as already provided by Koin (like DSL declaration, or internals)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class Provided

/**
 * Internal usage for components discovery in generated package
 *
 * @param value: package of declared definition
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
annotation class Definition(val value: String = "")