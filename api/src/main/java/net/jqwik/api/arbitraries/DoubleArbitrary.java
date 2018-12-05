package net.jqwik.api.arbitraries;

import net.jqwik.api.*;

/**
 * Fluent interface to configure the generation of Double and double values.
 */
public interface DoubleArbitrary extends Arbitrary<Double> {

	/**
	 * Set the allowed lower {@code min} (included) and upper {@code max} (included) bounder of generated numbers.
	 */
	default DoubleArbitrary between(double min, double max) {
		return greaterOrEqual(min).lessOrEqual(max);
	}

	/**
	 * Set the allowed lower {@code min} (included) bounder of generated numbers.
	 */
	DoubleArbitrary greaterOrEqual(double min);

	/**
	 * Set the allowed upper {@code max} (included) bounder of generated numbers.
	 */
	DoubleArbitrary lessOrEqual(double max);

	/**
	 * Set the scale (maximum number of decimal places) to {@code scale}.
	 */
	DoubleArbitrary ofScale(int scale);

}
