package net.jqwik.engine.properties.arbitraries;

import java.lang.reflect.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.configurators.*;
import net.jqwik.api.providers.*;
import net.jqwik.engine.properties.arbitraries.exhaustive.*;
import net.jqwik.engine.properties.shrinking.*;

public class DefaultArrayArbitrary<T, A> extends MultivalueArbitraryBase<T, A> implements SelfConfiguringArbitrary<A> {

	private final Class<A> arrayClass;

	public DefaultArrayArbitrary(Arbitrary<T> elementArbitrary, Class<A> arrayClass, boolean elementsUnique) {
		super(elementArbitrary, elementsUnique);
		this.arrayClass = arrayClass;
	}

	@Override
	public RandomGenerator<A> generator(int genSize) {
		return createListGenerator(genSize).map(this::toArray);
	}

	@Override
	public Optional<ExhaustiveGenerator<A>> exhaustive(long maxNumberOfSamples) {
		return ExhaustiveGenerators
				   .list(elementArbitrary, minSize, maxSize, maxNumberOfSamples)
				   .map(generator -> generator.map(this::toArray));
	}

	@Override
	public EdgeCases<A> edgeCases() {
		return edgeCases((elements, minSize1) -> new ShrinkableList<>(elements, minSize1, maxSize)).map(this::toArray);
	}

	@SuppressWarnings("unchecked")
	private A toArray(List<T> from) {
		A array = (A) Array.newInstance(arrayClass.getComponentType(), from.size());
		for (int i = 0; i < from.size(); i++) {
			Array.set(array, i, from.get(i));
		}
		return array;
	}

	@Override
	protected Iterable<T> toIterable(A array) {
		//noinspection unchecked
		return () -> Arrays.stream((T[]) array).iterator();
	}

	@Override
	public Arbitrary<A> configure(ArbitraryConfigurator configurator, TypeUsage targetType) {
		elementArbitrary = configurator.configure(elementArbitrary, targetType);
		return configurator.configure(this, targetType);
	}
}
