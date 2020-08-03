package net.jqwik.api;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.apiguardian.api.*;

import static org.apiguardian.api.API.Status.*;

@API(status = STABLE, since = "1.0")
public interface Shrinkable<T> extends Comparable<Shrinkable<T>> {

	@API(status = INTERNAL)
	abstract class ShrinkableFacade {
		private static final ShrinkableFacade implementation;

		static {
			implementation = FacadeLoader.load(ShrinkableFacade.class);
		}

		public abstract <T> Shrinkable<T> unshrinkable(Supplier<T> valueSupplier, ShrinkingDistance distance);

		public abstract <T, U> Shrinkable<U> map(Shrinkable<T> self, Function<T, U> mapper);

		public abstract <T> Shrinkable<T> filter(Shrinkable<T> self, Predicate<T> filter);

		public abstract <T, U> Shrinkable<U> flatMap(Shrinkable<T> self, Function<T, Arbitrary<U>> flatMapper, int tries, long randomSeed);
	}

	static <T> Shrinkable<T> unshrinkable(T value) {
		return unshrinkable(value, ShrinkingDistance.of(0));
	}

	static <T> Shrinkable<T> unshrinkable(T value, ShrinkingDistance distance) {
		return ShrinkableFacade.implementation.unshrinkable(() -> value, distance);
	}

	@API(status = INTERNAL)
	static <T> Shrinkable<T> supplyUnshrinkable(Supplier<T> supplier) {
		return ShrinkableFacade.implementation.unshrinkable(supplier, ShrinkingDistance.of(0));
	}

	/**
	 * Create value freshly, so that in case of mutable objects shrinking (and reporting)
	 * can rely on untouched values.
	 *
	 * @return An un-changed instance of the value represented by this shrinkable
	 */
	T value();

	/**
	 * Create a new and finite stream of smaller or same size shrinkables; size is measured by {@linkplain #distance()}.
	 * <p>
	 * Same size shrinkables are allowed but they have to iterate towards a single value to prevent endless shrinking.
	 * This also means that a shrinkable must never be in its own shrink stream!
	 */
	@API(status = INTERNAL, since = "1.3.3")
	Stream<Shrinkable<T>> shrink();

	/**
	 * To be able to "move" values towards the end of collections while keeping some constraint constant
	 * it's necessary to grow a shrinkable by what another has been shrunk.
	 * One example is keeping a sum of values and still shrinking to the same resulting list.
	 *
	 * @param before The other shrinkable before shrinking
	 * @param after The other shrinkable after shrinking
	 * @return this shrinkable grown by the difference of before and after
	 */
	@API(status = INTERNAL, since = "1.3.3")
	default Optional<Shrinkable<T>> grow(Shrinkable<T> before, Shrinkable<T> after) {
		return Optional.empty();
	}

	/**
	 * This method is no longer used anywhere within jqwik.
	 *
	 * @deprecated Will be removed in version 1.4
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.3.3")
	default ShrinkingSequence<T> shrink(Falsifier<T> falsifier) {
		throw new JqwikException("This method must not be used any more");
	}

	ShrinkingDistance distance();

	/**
	 * Sometimes simplifies test writing
	 *
	 * @return generic version of a shrinkable
	 */
	@SuppressWarnings("unchecked")
	@API(status = INTERNAL, since = "1.2.4")
	default Shrinkable<Object> asGeneric() {
		return (Shrinkable<Object>) this;
	}

	default <U> Shrinkable<U> map(Function<T, U> mapper) {
		return ShrinkableFacade.implementation.map(this, mapper);
	}

	default Shrinkable<T> filter(Predicate<T> filter) {
		return ShrinkableFacade.implementation.filter(this, filter);
	}

	default <U> Shrinkable<U> flatMap(Function<T, Arbitrary<U>> flatMapper, int tries, long randomSeed) {
		return ShrinkableFacade.implementation.flatMap(this, flatMapper, tries, randomSeed);
	}

	@SuppressWarnings("unchecked")
	@Override
	@API(status = INTERNAL)
	default int compareTo(Shrinkable<T> other) {
		int comparison = this.distance().compareTo(other.distance());
		if (comparison == 0) {
			T value = value();
			if (value instanceof Comparable && this.getClass().equals(other.getClass())) {
				return ((Comparable<T>) value).compareTo(other.value());
			}
		}
		return comparison;
	}

	@API(status = INTERNAL)
	default Shrinkable<T> makeUnshrinkable() {
		return ShrinkableFacade.implementation.unshrinkable(this::value, this.distance());
	}

}
