package net.jqwik.engine.facades;

import java.util.*;
import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.engine.properties.arbitraries.randomized.*;
import net.jqwik.engine.properties.shrinking.*;

/**
 * Is loaded through reflection in api module
 */
public class RandomGeneratorFacadeImpl extends RandomGenerator.RandomGeneratorFacade {
	@Override
	public <T, U> Shrinkable<U> flatMap(Shrinkable<T> self, Function<T, RandomGenerator<U>> mapper, long nextLong) {
		return new FlatMappedShrinkable<>(self, mapper, nextLong);
	}

	@Override
	public <T, U> Shrinkable<U> flatMap(Shrinkable<T> self, Function<T, Arbitrary<U>> mapper, int genSize, long nextLong) {
		return new FlatMappedShrinkable<>(self, mapper, genSize, nextLong);
	}

	@Override
	public <T> RandomGenerator<T> filter(RandomGenerator<T> self, Predicate<T> filterPredicate) {
		return new FilteredGenerator<>(self, filterPredicate);
	}

	@Override
	public <T> RandomGenerator<T> withEdgeCases(RandomGenerator<T> self, int genSize, EdgeCases<T> edgeCases) {
		return RandomGenerators.withEdgeCases(self, genSize, edgeCases);
	}

	@Override
	public <T> RandomGenerator<T> unique(RandomGenerator<T> self) {
		return new UniqueGenerator<>(self);
	}

	@Override
	public <T> RandomGenerator<List<T>> collect(RandomGenerator<T> self, Predicate<List<T>> until) {
		return new CollectGenerator<>(self, until);
	}

	@Override
	public <T> RandomGenerator<T> injectDuplicates(RandomGenerator<T> self, double duplicateProbability) {
		return new InjectDuplicatesGenerator(self, duplicateProbability);
	}
}
