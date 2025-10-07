---
title: KSP Compiler Options
---

The Koin Annotations KSP processor supports several configuration options that can be passed during compilation to customize code generation behavior.

## Available Options

### KOIN_CONFIG_CHECK
- **Type**: Boolean
- **Default**: `false`
- **Description**: Enables compile-time configuration checking for Koin definitions. When enabled, the compiler will validate all Koin configurations at compile time to ensure safety and catch potential issues early.
- **Usage**: Helps with compile-time safety by detecting configuration problems before runtime.

### KOIN_LOG_TIMES
- **Type**: Boolean
- **Default**: `false`
- **Description**: Displays timing logs for module generation during compilation. This helps monitor the performance of code generation and identify potential bottlenecks.
- **Usage**: Useful for debugging and optimizing build times.

### KOIN_DEFAULT_MODULE
- **Type**: Boolean
- **Default**: `false`
- **Status**: ⚠️ **Deprecated since 1.3.0**
- **Description**: Automatically generates a default module if no explicit module is found for a given definition. **This option is deprecated since Annotations 1.3.0 and discouraged.** Instead, use the `@Configuration` annotation and `@KoinApplication` to bootstrap your application automatically.
- **Usage**: Avoid using this option. Prefer explicit module organization with `@Configuration` and `@KoinApplication` for better code clarity and maintainability.

### KOIN_GENERATION_PACKAGE
- **Type**: String
- **Default**: `"org.koin.ksp.generated"`
- **Description**: Specifies the package name where generated Koin classes will be placed. The package name must be a valid Kotlin package identifier. **Important**: This option must be used consistently across all modules with the same value if set.
- **Usage**: Only use this option when your project requires generating code in a different path than the default (e.g., due to specific coding rules or project structure requirements). Ensure all modules use the same package name.

### KOIN_USE_COMPOSE_VIEWMODEL
- **Type**: Boolean
- **Default**: `true`
- **Description**: Generates ViewModel definitions using `koin-core-viewmodel` main DSL instead of the Android-specific ViewModel. This is enabled by default to provide Kotlin Multiplatform compatibility and use the unified ViewModel API.
- **Usage**: Recommended to keep enabled for all projects. Essential for KMP projects that need ViewModel support across platforms.

### KOIN_EXPORT_DEFINITIONS
- **Type**: Boolean
- **Default**: `true`
- **Description**: Controls whether exported definitions are generated in addition to module-assembled definitions. When disabled, only definitions assembled in modules will be generated, filtering out standalone exported definitions.
- **Usage**: Set to `false` if you want to only generate definitions that are explicitly assembled in modules and exclude standalone exported definitions. Useful for stricter module organization.

## Configuration Examples

### Gradle Kotlin DSL

```kotlin
ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_LOG_TIMES", "true")
    arg("KOIN_DEFAULT_MODULE", "false")
    arg("KOIN_GENERATION_PACKAGE", "com.mycompany.koin.generated")
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
    arg("KOIN_EXPORT_DEFINITIONS", "true")
}
```

### Gradle Groovy DSL

```groovy
ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_LOG_TIMES", "true")
    arg("KOIN_DEFAULT_MODULE", "false")
    arg("KOIN_GENERATION_PACKAGE", "com.mycompany.koin.generated")
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
    arg("KOIN_EXPORT_DEFINITIONS", "true")
}
```

## Best Practices

- **Enable KOIN_CONFIG_CHECK** in development builds to catch configuration issues early
- **Use KOIN_LOG_TIMES** during build optimization to identify performance bottlenecks
- **Only use KOIN_GENERATION_PACKAGE** when necessary for coding rules compliance - ensure consistent usage across all modules
- **Keep KOIN_USE_COMPOSE_VIEWMODEL** enabled (default) for unified ViewModel API across platforms
- **Avoid KOIN_DEFAULT_MODULE** - use `@Configuration` and `@KoinApplication` for proper application bootstrap

## Package Name Validation

When using `KOIN_GENERATION_PACKAGE`, the provided package name must:
- Not be empty
- Contain only valid Kotlin identifiers separated by dots
- Not use Kotlin keywords or reserved words
- Follow standard Java/Kotlin package naming conventions

Invalid package names will result in compilation errors with descriptive messages.