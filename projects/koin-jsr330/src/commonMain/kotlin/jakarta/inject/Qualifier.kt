package jakarta.inject

/**
 * - Backported from jakarta inject -
 *
 * Identifies qualifier annotations. Anyone can define a new qualifier. A
 * qualifier annotation:
 *
 * <ul>
 *   <li>is annotated with {@code @Qualifier}, {@code @Retention(RUNTIME)},
 *      and typically {@code @Documented}.</li>
 *   <li>can have attributes.</li>
 *   <li>may be part of the public API, much like the dependency type, but
 *      unlike implementation types which needn't be part of the public
 *      API.</li>
 *   <li>may have restricted usage if annotated with {@code @Target}. While
 *      this specification covers applying qualifiers to fields and
 *      parameters only, some injector configurations might use qualifier
 *      annotations in other places (on methods or classes for example).</li>
 * </ul>
 *
 * <p>For example:
 *
 * <pre>
 *   &#064;java.lang.annotation.Documented
 *   &#064;java.lang.annotation.Retention(RUNTIME)
 *   &#064;jakarta.inject.Qualifier
 *   public @interface Leather {
 *     Color color() default Color.TAN;
 *     public enum Color { RED, BLACK, TAN }
 *   }</pre>
 *
 * @see jakarta.inject.Named @Named
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class Qualifier()
