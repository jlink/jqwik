package net.jqwik.engine.properties.shrinking;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.support.*;

public class CombinedShrinkable<T> implements Shrinkable<T> {
	private final List<Shrinkable<Object>> shrinkables;
	private final Function<List<Object>, T> combinator;
	private final T value;

	public CombinedShrinkable(List<Shrinkable<Object>> shrinkables, Function<List<Object>, T> combinator) {
		this.shrinkables = shrinkables;
		this.combinator = combinator;
		this.value = combinator.apply(toValues(shrinkables));
	}

	@Override
	public T value() {
		// TODO: Compute lazy. Requires change of FlatMappedShrinkable
		return value;
	}

	@Override
	public T createValue() {
		return combinator.apply(toValues(shrinkables));
	}

	private List<Object> toValues(List<Shrinkable<Object>> shrinkables) {
		return shrinkables.stream().map(Shrinkable::createValue).collect(Collectors.toList());
	}

	@Override
	public ShrinkingSequence<T> shrink(Falsifier<T> falsifier) {
		return new CombinedShrinkingSequence(falsifier);
	}

	@Override
	public ShrinkingDistance distance() {
		return ShrinkingDistance.combine(shrinkables);
	}

	private class CombinedShrinkingSequence implements ShrinkingSequence<T> {

		final private ShrinkingSequence<List<Object>> elementsSequence;

		private CombinedShrinkingSequence(Falsifier<T> falsifier) {
			Falsifier<List<Object>> combinedFalsifier = falsifier.map(combinator);
			elementsSequence = new ShrinkElementsSequence<>(
				shrinkables,
				combinedFalsifier,
				ShrinkingDistance::combine
			);
		}

		@Override
		public void init(FalsificationResult<T> initialCurrent) {
			// Only throwable is used in elementsSequence
			elementsSequence.init(FalsificationResult.falsified(
				Shrinkable.unshrinkable(new ArrayList<>()),
				initialCurrent.throwable().orElse(null)
			));
		}

		@Override
		public boolean next(Runnable count, Consumer<FalsificationResult<T>> falsifiedReporter) {
			Consumer<FalsificationResult<List<Object>>> combinedReporter =
				result -> falsifiedReporter.accept(result.map(shrinkable -> shrinkable.map(combinator)));
			return elementsSequence.next(count, combinedReporter);
		}

		@Override
		public FalsificationResult<T> current() {
			return elementsSequence.current().map(shrinkable -> shrinkable.map(combinator));
		}
	}
}
