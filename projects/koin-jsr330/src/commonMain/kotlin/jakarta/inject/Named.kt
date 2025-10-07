package jakarta.inject

/**
 * - Backported from jakarta inject -
 *
 * String-based {@linkplain qualifier}.
 *
 * <p>Example usage:
 *
 * <pre>
 *   public class Car {
 *     &#064;Inject <b>@Named("driver")</b> Seat driverSeat;
 *     &#064;Inject <b>@Named("passenger")</b> Seat passengerSeat;
 *     ...
 *   }</pre>
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class Named(val value: String = "")
