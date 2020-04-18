package net.jqwik.engine.properties.arbitraries;

import java.math.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.engine.properties.*;

public class DefaultBigDecimalArbitrary extends AbstractArbitraryBase implements BigDecimalArbitrary {

	private static final BigDecimal DEFAULT_MIN = BigDecimal.valueOf(-Double.MAX_VALUE);
	private static final BigDecimal DEFAULT_MAX = BigDecimal.valueOf(Double.MAX_VALUE);
	private static final Range<BigDecimal> DEFAULT_RANGE = Range.of(DEFAULT_MIN, DEFAULT_MAX);

	private final DecimalGeneratingArbitrary generatingArbitrary;

	public DefaultBigDecimalArbitrary() {
		this.generatingArbitrary = new DecimalGeneratingArbitrary(DEFAULT_RANGE);
	}

	@Override
	public RandomGenerator<BigDecimal> generator(int genSize) {
		return generatingArbitrary.generator(genSize);
	}

	@Override
	public Optional<ExhaustiveGenerator<BigDecimal>> exhaustive(long maxNumberOfSamples) {
		return generatingArbitrary.exhaustive(maxNumberOfSamples);
	}

	@Override
	public EdgeCases<BigDecimal> edgeCases() {
		return generatingArbitrary.edgeCases();
	}

	@Override
	public BigDecimalArbitrary between(BigDecimal min, boolean minIncluded, BigDecimal max, boolean maxIncluded) {
		min = (min == null) ? DEFAULT_MIN : min;
		max = (max == null) ? DEFAULT_MAX : max;
		DefaultBigDecimalArbitrary clone = typedClone();
		clone.generatingArbitrary.range = Range.of(min, minIncluded, max, maxIncluded);
		return clone;
	}

	@Override
	public BigDecimalArbitrary lessThan(BigDecimal max) {
		return between(generatingArbitrary.range.min, generatingArbitrary.range.minIncluded, max, false);
	}

	@Override
	public BigDecimalArbitrary lessOrEqual(BigDecimal max) {
		return between(generatingArbitrary.range.min, generatingArbitrary.range.minIncluded, max, true);
	}

	@Override
	public BigDecimalArbitrary greaterOrEqual(BigDecimal min) {
		return between(min, true, generatingArbitrary.range.max, generatingArbitrary.range.maxIncluded);
	}

	@Override
	public BigDecimalArbitrary greaterThan(BigDecimal min) {
		return between(min, false, generatingArbitrary.range.max, generatingArbitrary.range.maxIncluded);
	}

	@Override
	public BigDecimalArbitrary ofScale(int scale) {
		DefaultBigDecimalArbitrary clone = typedClone();
		clone.generatingArbitrary.scale = scale;
		return clone;
	}

	@Override
	public BigDecimalArbitrary shrinkTowards(BigDecimal target) {
		DefaultBigDecimalArbitrary clone = typedClone();
		clone.generatingArbitrary.shrinkingTarget = target;
		return clone;
	}

}
