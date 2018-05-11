package net.jqwik.properties.shrinking;

import net.jqwik.api.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class DeepSearchShrinkingSequence<T> implements ShrinkingSequence<T> {
	private final Function<NShrinkable<T>, Set<NShrinkable<T>>> candidatesFor;
	private final Falsifier<T> falsifier;
	private FalsificationResult<T> currentBest;
	private FalsificationResult<T> searchBase;
	private boolean lastStepSuccessful = true;

	public DeepSearchShrinkingSequence(NShrinkable<T> startingShrinkable, Function<NShrinkable<T>, Set<NShrinkable<T>>> candidatesFor, Falsifier<T> falsifier) {
		this.currentBest = FalsificationResult.falsified(startingShrinkable);
		this.searchBase = currentBest;
		this.candidatesFor = candidatesFor;
		this.falsifier = falsifier;
	}

	@Override
	public boolean next(Runnable count, Consumer<T> reportFalsified) {
		if (!lastStepSuccessful)
			return false;

		lastStepSuccessful = false;

		Set<NShrinkable<T>> candidates = getShrinkableCandidates();

		List<FalsificationResult<T>> nextBase = candidates
			.stream()
			.sorted()
			.map(this::falsify)
			.filter(result -> result.status() != FalsificationResult.Status.VERIFIED)
			.collect(Collectors.toList());

		nextBase
			.stream()
			.filter(result -> result.status() == FalsificationResult.Status.FALSIFIED)
			.findFirst()
			.ifPresent(result -> {
				count.run();
				lastStepSuccessful = true;
				this.currentBest = result;
				reportFalsified.accept(this.currentBest.value());
				this.searchBase = this.currentBest;
			});

		nextBase
			.stream()
			.filter(result -> result.status() == FalsificationResult.Status.FILTERED_OUT)
			.filter(result -> result.shrinkable().isSmallerThan(currentBest.shrinkable())).filter(result -> result.shrinkable().isSmallerThan(searchBase.shrinkable()))
			.findFirst()
			.ifPresent(result -> {
				count.run();
				lastStepSuccessful = true;
				searchBase = result;
			});

		return lastStepSuccessful;
	}

	private Set<NShrinkable<T>> getShrinkableCandidates() {
		Set<NShrinkable<T>> candidates = new HashSet<>(candidatesFor.apply(searchBase.shrinkable()));
		if (searchBase != currentBest) {
			candidates.addAll(candidatesFor.apply(currentBest.shrinkable()));
		}
		return candidates;
	}

	private FalsificationResult<T> falsify(NShrinkable<T> candidate) {
		return falsifier.falsify(candidate);
	}

	@Override
	public FalsificationResult<T> current() {
		return currentBest;
	}
}
