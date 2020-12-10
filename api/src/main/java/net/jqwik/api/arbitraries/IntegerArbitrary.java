package net.jqwik.api.arbitraries;

import org.apiguardian.api.*;

import static org.apiguardian.api.API.Status.*;

/**
 * Fluent interface to configure the generation of Integer and int values.
 */
@API(status = MAINTAINED, since = "1.0")
public interface IntegerArbitrary extends NumericalArbitrary<Integer, IntegerArbitrary> {

	/**
	 * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounder of generated numbers.
	 */
	default IntegerArbitrary between(int min, int max) {
		return greaterOrEqual(min).lessOrEqual(max);
	}

	/**
	 * Set the allowed lower {@code min} (included) bounder of generated numbers.
	 */
	IntegerArbitrary greaterOrEqual(int min);

	/**
	 * Set the allowed upper {@code max} (included) bounder of generated numbers.
	 */
	IntegerArbitrary lessOrEqual(int max);

	/**
	 * Set shrinking target to {@code target} which must be between the allowed bounds.
	 */
	@API(status = MAINTAINED, since = "1.4.0")
	IntegerArbitrary shrinkTowards(int target);
}
