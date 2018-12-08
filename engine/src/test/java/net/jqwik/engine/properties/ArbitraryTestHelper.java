package net.jqwik.engine.properties;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.*;

public class ArbitraryTestHelper {

	@SafeVarargs
	public static <T> void assertAtLeastOneGeneratedOf(RandomGenerator<T> generator, T... values) {
		for (T value : values) {
			assertAtLeastOneGenerated(generator, value::equals, "Failed to generate " + value);
		}
	}

	public static <T> void assertAtLeastOneGenerated(RandomGenerator<T> generator, Function<T, Boolean> checker) {
		assertAtLeastOneGenerated(generator, checker, "Failed to generate at least one");
	}

	public static <T> Shrinkable<T> generateValueUntil(RandomGenerator<T> generator, Function<T, Boolean> condition) {
		long maxTries = 1000;
		return generator
				   .stream(SourceOfRandomness.current())
				   .limit(maxTries)
				   .filter(shrinkable -> condition.apply(shrinkable.value()))
				   .findFirst()
				   .orElseThrow(() -> new JqwikException("Failed to generate value that fits condition after " + maxTries + " tries."));
	}

	public static <T> Map<T, Long> count(RandomGenerator<T> generator, int tries) {
		return generator
				   .stream(SourceOfRandomness.current())
				   .limit(tries)
				   .map(Shrinkable::value)
				   .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	public static <T> void assertAtLeastOneGenerated(RandomGenerator<T> generator, Function<T, Boolean> checker, String failureMessage) {
		Random random = SourceOfRandomness.current();
		Optional<Shrinkable<T>> success = generator
											  .stream(random)
											  .limit(3000)
											  .filter(shrinkable -> checker.apply(shrinkable.value()))
											  .findAny();
		if (!success.isPresent()) {
			fail(failureMessage);
		}
	}

	public static <T> void assertAllGenerated(RandomGenerator<T> generator, Predicate<T> checker) {
		Random random = SourceOfRandomness.current();
		Optional<Shrinkable<T>> failure = generator
											  .stream(random)
											  .limit(100)
											  .filter(shrinkable -> !checker.test(shrinkable.value()))
											  .findAny();

		failure.ifPresent(shrinkable -> {
			fail(String.format("Value [%s] failed to fulfill condition.", shrinkable.value().toString()));
		});
	}

	public static <T> void assertAllGenerated(RandomGenerator<T> generator, Consumer<T> assertions) {
		Predicate<T> checker = value -> {
			try {
				assertions.accept(value);
				return true;
			} catch (Throwable any) {
				return false;
			}
		};
		assertAllGenerated(generator, checker);
	}

	@SafeVarargs
	public static <T> void assertGeneratedExactly(RandomGenerator<T> generator, T... expectedValues) {
		Random random = SourceOfRandomness.current();

		List<T> generated = generator
								.stream(random)
								.limit(expectedValues.length)
								.map(Shrinkable::value)
								.collect(Collectors.toList());

		assertThat(generated).containsExactly(expectedValues);
	}

	public static <T> void assertAllValuesAreShrunkTo(T expectedShrunkValue, Arbitrary<T> arbitrary, Random random) {
		T value = shrinkToEnd(arbitrary, random);
		assertThat(value).isEqualTo(expectedShrunkValue);
	}

	public static <T> T shrinkToEnd(Arbitrary<T> arbitrary, Random random) {
		Shrinkable<T> shrinkable = arbitrary.generator(10).next(random);
		ShrinkingSequence<T> sequence = shrinkable.shrink(value -> false);
		while(sequence.next(() -> {}, ignore -> {}));
		return sequence.current().value();
	}

}
