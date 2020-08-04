package net.jqwik.engine.properties.arbitraries;

import java.util.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.engine.properties.arbitraries.exhaustive.*;
import net.jqwik.engine.properties.shrinking.*;

public class DefaultStreamArbitrary<T> extends MultivalueArbitraryBase<T, Stream<T>> implements StreamArbitrary<T> {

	public DefaultStreamArbitrary(Arbitrary<T> elementArbitrary, boolean elementsUnique) {
		super(elementArbitrary, elementsUnique);
	}

	@Override
	protected Iterable<T> toIterable(Stream<T> streamable) {
		return streamable::iterator;
	}

	@Override
	public RandomGenerator<Stream<T>> generator(int genSize) {
		return createListGenerator(genSize).map(ReportableStream::new);
	}

	@Override
	public Optional<ExhaustiveGenerator<Stream<T>>> exhaustive(long maxNumberOfSamples) {
		return ExhaustiveGenerators
				   .list(elementArbitrary, minSize, maxSize, maxNumberOfSamples)
				   .map(generator -> generator.map(ReportableStream::new));
	}

	@Override
	public EdgeCases<Stream<T>> edgeCases() {
		return edgeCases((elements, minSize1) -> new ShrinkableList<>(elements, minSize1, maxSize)).map(Collection::stream);
	}

	@Override
	public StreamArbitrary<T> ofMaxSize(int maxSize) {
		return (StreamArbitrary<T>) super.ofMaxSize(maxSize);
	}

	@Override
	public StreamArbitrary<T> ofMinSize(int minSize) {
		return (StreamArbitrary<T>) super.ofMinSize(minSize);
	}
}
