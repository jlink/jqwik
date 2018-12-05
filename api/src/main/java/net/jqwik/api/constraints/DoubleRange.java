package net.jqwik.api.constraints;

import java.lang.annotation.*;

/**
 * Constrain the range of a generated double or Double parameters.
 *
 * Applies to double or Double parameters which are also annotated with {@code @ForAll}.
 *
 * @see net.jqwik.api.ForAll
 * @see ByteRange
 * @see ShortRange
 * @see IntRange
 * @see LongRange
 * @see FloatRange
 * @see BigRange
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoubleRange {
	double min() default 0.0;

	double max();
}
