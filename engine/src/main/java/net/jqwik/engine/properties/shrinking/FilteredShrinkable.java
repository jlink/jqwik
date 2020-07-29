package net.jqwik.engine.properties.shrinking;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;

public class FilteredShrinkable<T> implements Shrinkable<T> {

	private final Shrinkable<T> toFilter;
	private final Predicate<T> filter;

	public FilteredShrinkable(Shrinkable<T> toFilter, Predicate<T> filter) {
		this.toFilter = toFilter;
		this.filter = filter;
	}

	@Override
	public T value() {
		return toFilter.value();
	}

	@Override
	public T createValue() {
		return toFilter.createValue();
	}

	@Override
	public Stream<Shrinkable<T>> shrink() {
		return shrinkToFirst();
	}

	private Stream<Shrinkable<T>> shrinkToFirst() {
		return toFilter.shrink()
					   .filter(this::isIncluded)
					   .findFirst()
					   .map(t -> Stream.of(toFiltered(t)))
					   .orElse(deepSearchFirst());
	}

	private Stream<Shrinkable<T>> deepSearchFirst() {
		return toFilter.shrink()
					   .flatMap(Shrinkable::shrink)
					   .flatMap(shrinkable -> {
						   if (isIncluded(shrinkable)) {
							   return Stream.of(shrinkable);
						   } else {
							   // Is the limit necessary?
							   return shrinkable.shrink().limit(10);
						   }
					   })
					   .findFirst()
					   .map(t -> Stream.of(toFiltered(t)))
					   .orElse(Stream.empty());
	}

	private boolean isIncluded(Shrinkable<T> shrinkable) {
		return filter.test(shrinkable.createValue());
	}

	private Shrinkable<T> toFiltered(Shrinkable<T> t) {
		return new FilteredShrinkable<>(t, filter);
	}

	@Override
	public ShrinkingSequence<T> shrink(Falsifier<T> falsifier) {
		return new FilteredShrinkingSequence(falsifier);
	}

	@Override
	public List<Shrinkable<T>> shrinkingSuggestions() {
		return toFilter.shrinkingSuggestions()
					   .stream()
					   .filter(shrinkable -> filter.test(shrinkable.value()))
					   .collect(Collectors.toList());
	}

	@Override
	public ShrinkingDistance distance() {
		return toFilter.distance();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FilteredShrinkable<?> that = (FilteredShrinkable<?>) o;
		return toFilter.equals(that.toFilter);
	}

	@Override
	public int hashCode() {
		return toFilter.hashCode();
	}

	@Override
	public String toString() {
		return String.format("Filtered|%s", toFilter);
	}

	private class FilteredShrinkingSequence implements ShrinkingSequence<T> {

		private final ShrinkingSequence<T> toFilterSequence;

		private FilteredShrinkingSequence(Falsifier<T> falsifier) {
			Falsifier<T> filteredFalsifier = falsifier.withFilter(filter);
			toFilterSequence = toFilter.shrink(filteredFalsifier);
		}

		@Override
		public boolean next(Runnable count, Consumer<FalsificationResult<T>> falsifiedReporter) {
			return toFilterSequence.next(count, falsifiedReporter);
		}

		@Override
		public FalsificationResult<T> current() {
			return toFilterSequence.current().filter(filter);
		}

		@Override
		public void init(FalsificationResult<T> initialCurrent) {
			toFilterSequence.init(initialCurrent);
		}
	}
}
