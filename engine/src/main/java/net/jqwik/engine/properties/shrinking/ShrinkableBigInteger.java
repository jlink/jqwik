package net.jqwik.engine.properties.shrinking;

import java.math.*;
import java.util.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.engine.properties.*;
import net.jqwik.engine.support.*;

public class ShrinkableBigInteger extends AbstractValueShrinkable<BigInteger> {
	private final Range<BigInteger> range;
	private final BigInteger shrinkingTarget;

	public ShrinkableBigInteger(BigInteger value, Range<BigInteger> range, BigInteger shrinkingTarget) {
		super(value);
		this.range = range;
		this.shrinkingTarget = shrinkingTarget;
		checkValueInRange(value);
	}

	@Override
	public Stream<Shrinkable<BigInteger>> shrink() {
		return JqwikStreamSupport.concat(
			shrinkTowardsTarget(this),
			shrinkNegativeToPositive(this)
		);
	}

	@Override
	public Optional<Shrinkable<BigInteger>> grow(Shrinkable<?> before, Shrinkable<?> after) {
		Object beforeValue = before.value();
		Object afterValue = after.value();
		BigInteger diff = calculateDiff(beforeValue, afterValue);
		if (diff.compareTo(BigInteger.ZERO) != 0) {
			BigInteger grownValue = value().add(diff);
			if (range.includes(grownValue)) {
				return Optional.of(new ShrinkableBigInteger(grownValue, range, shrinkingTarget));
			}
		}
		return Optional.empty();
	}

	private BigInteger calculateDiff(Object beforeValue, Object afterValue) {
		if (beforeValue instanceof BigInteger && afterValue instanceof BigInteger) {
			return ((BigInteger) beforeValue).subtract((BigInteger) afterValue);
		}
		try {
			return BigInteger.valueOf(toLong(beforeValue) - toLong(afterValue));
		} catch (ClassCastException notAnIntegral) {
			return BigInteger.ZERO;
		}
	}

	private long toLong(Object value) {
		// TODO: Cover also Long, Short and Byte
		return ((Integer) value).longValue();
	}

	private Stream<Shrinkable<BigInteger>> shrinkNegativeToPositive(Shrinkable<BigInteger> shrinkable) {
		if (shrinkable.value().compareTo(BigInteger.ZERO) >= 0) {
			return Stream.empty();
		}
		return Stream.of(shrinkable)
					 .map(s -> shrinkable.value().negate())
					 .filter(range::includes)
					 .map(this::createShrinkable);
	}

	private Stream<Shrinkable<BigInteger>> shrinkTowardsTarget(Shrinkable<BigInteger> shrinkable) {
		return new BigIntegerShrinker(shrinkingTarget)
				   .shrink(shrinkable.value())
				   .map(this::createShrinkable)
				   .sorted(Comparator.comparing(Shrinkable::distance));
	}

	private Shrinkable<BigInteger> createShrinkable(BigInteger aBigInteger) {
		return new ShrinkableBigInteger(aBigInteger, range, shrinkingTarget);
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
