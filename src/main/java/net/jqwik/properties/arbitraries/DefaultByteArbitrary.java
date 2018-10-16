package net.jqwik.properties.arbitraries;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;

import java.math.*;
import java.util.*;

public class DefaultByteArbitrary extends AbstractArbitraryBase implements ByteArbitrary {

	private static final byte DEFAULT_MIN = Byte.MIN_VALUE;
	private static final byte DEFAULT_MAX = Byte.MAX_VALUE;

	private final IntegralGeneratingArbitrary generatingArbitrary;

	public DefaultByteArbitrary() {
		this.generatingArbitrary = new IntegralGeneratingArbitrary(BigInteger.valueOf(DEFAULT_MIN), BigInteger.valueOf(DEFAULT_MAX));
	}

	@Override
	public RandomGenerator<Byte> generator(int genSize) {
		return generatingArbitrary.generator(genSize).map(BigInteger::byteValueExact);
	}

	@Override
	public Optional<ExhaustiveGenerator<Byte>> exhaustive() {
		return generatingArbitrary.exhaustive().map(generator -> generator.map(BigInteger::byteValueExact));
	}

	@Override
	public ByteArbitrary greaterOrEqual(byte min) {
		DefaultByteArbitrary clone = typedClone();
		clone.generatingArbitrary.min = BigInteger.valueOf(min);
		return clone;
	}

	@Override
	public ByteArbitrary lessOrEqual(byte max) {
		DefaultByteArbitrary clone = typedClone();
		clone.generatingArbitrary.max = BigInteger.valueOf(max);
		return clone;
	}

}
