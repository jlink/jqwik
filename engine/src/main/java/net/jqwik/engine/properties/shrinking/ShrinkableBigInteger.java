package net.jqwik.engine.properties.shrinking;

import java.math.*;
import java.util.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.engine.properties.*;

public class ShrinkableBigInteger extends AbstractShrinkable<BigInteger> {
	private final Range<BigInteger> range;
	private final BigInteger shrinkingTarget;

	public ShrinkableBigInteger(BigInteger value, Range<BigInteger> range, BigInteger shrinkingTarget) {
		super(value);
		this.range = range;
		this.shrinkingTarget = shrinkingTarget;
		checkValueInRange(value);
	}

	@Override
	public Set<Shrinkable<BigInteger>> shrinkCandidatesFor(Shrinkable<BigInteger> shrinkable) {
		return new BigIntegerShrinker(shrinkingTarget)
				   .shrink(shrinkable.value())
				   .map(aBigInteger -> new ShrinkableBigInteger(aBigInteger, range, shrinkingTarget))
				   .collect(Collectors.toSet());
	}

	@Override
	public ShrinkingDistance distance() {
		return distanceFor(value(), shrinkingTarget);
	}

	static ShrinkingDistance distanceFor(BigInteger value, BigInteger target) {
		BigInteger distance = value.subtract(target).abs();
		if (distance.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0) return ShrinkingDistance.of(Long.MAX_VALUE);
		return ShrinkingDistance.of(distance.longValueExact());
	}

	private void checkValueInRange(BigInteger value) {
		if (!range.includes(value)) {
			String message = String.format("Value <%s> is outside allowed range %s", value, range);
			throw new JqwikException(message);
		}
	}

}
