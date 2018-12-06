package net.jqwik.engine.properties.arbitraries.exhaustive;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.engine.support.*;

import static net.jqwik.engine.support.MathSupport.*;

class PermutationExhaustiveGenerator<T> implements ExhaustiveGenerator<List<T>> {
	private final List<T> values;
	private final Long maxCount;

	public PermutationExhaustiveGenerator(List<T> values, Long maxCount) {
		this.values = values;
		this.maxCount = maxCount;
	}

	static <T> Optional<Long> calculateMaxCount(List<T> values) {
		long choices = factorial(values.size());
		if (choices > ExhaustiveGenerators.MAXIMUM_ACCEPTED_MAX_COUNT || choices < 0) {
			return Optional.empty();
		}
		return Optional.of(choices);
	}

	@Override
	public long maxCount() {
		return maxCount;
	}

	@Override
	public Iterator<List<T>> iterator() {
		return Combinatorics.listPermutations(values);
	}
}
