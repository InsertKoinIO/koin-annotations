package jakarta.inject

/**
 * - Backported from jakarta inject -
 *
 * Identifies a type that the injector only instantiates once. Not inherited.
 *
 * @see jakarta.inject.Scope @Scope
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Singleton()
