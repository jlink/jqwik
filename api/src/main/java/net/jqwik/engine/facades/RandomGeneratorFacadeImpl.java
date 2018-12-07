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
	public <T> RandomGenerator<T> withEdgeCases(RandomGenerator<T> self, int genSize, List<Shrinkable<T>> edgeCases) {
		return RandomGenerators.withEdgeCases(self, genSize, edgeCases);
	}

	@Override
	public <T> RandomGenerator<T> withSamples(RandomGenerator<T> self, T[] samples) {
		return new WithSamplesGenerator<>(self, samples);
	}

	@Override
	public <T> RandomGenerator<T> unique(RandomGenerator<T> self) {
		return new UniqueGenerator<>(self);
	}
}
