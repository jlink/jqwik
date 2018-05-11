package net.jqwik.properties.shrinking;

import net.jqwik.api.*;

import java.util.*;

public abstract class AbstractShrinkable<T> implements NShrinkable<T> {

	private final T value;

	public AbstractShrinkable(T value) {
		this.value = value;
	}

	@Override
	public T value() {
		return value;
	}

	@Override
	public ShrinkingSequence<T> shrink(Falsifier<T> falsifier) {
		return new DeepSearchShrinkingSequence<>(this, this::shrinkCandidatesFor, falsifier);
	}

	public abstract Set<NShrinkable<T>> shrinkCandidatesFor(NShrinkable<T> shrinkable);

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AbstractShrinkable<?> that = (AbstractShrinkable<?>) o;

		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return String.format("%s<%s>(%s:%s)", //
			getClass().getSimpleName(), //
			value().getClass().getSimpleName(), //
			value(), distance());
	}
}
