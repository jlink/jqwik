package net.jqwik.engine.properties.shrinking;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.engine.properties.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.api.ArbitraryTestHelper.*;

class ArbitraryShrinkingTests {

	@Property(tries = 10)
	void values(@ForAll Random random) {
		Arbitrary<Integer> arbitrary = Arbitraries.of(1, 2, 3);
		assertAllValuesAreShrunkTo(1, arbitrary, random);
	}

	@Property(tries = 10)
	void filtered(@ForAll Random random) {
		Arbitrary<Integer> arbitrary =
			Arbitraries.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).filter(i -> i % 2 == 0);
		assertAllValuesAreShrunkTo(2, arbitrary, random);
	}

	@Property(tries = 10)
	void unique(@ForAll Random random) {
		Arbitrary<Integer> arbitrary =
			Arbitraries.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).unique();
		assertAllValuesAreShrunkTo(1, arbitrary, random);
	}

	@Property(tries = 10)
	void uniqueInSet(@ForAll Random random) {
		Arbitrary<Set<Integer>> arbitrary =
			Arbitraries.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).unique().set().ofSize(3);
		assertAllValuesAreShrunkTo(new HashSet<>(Arrays.asList(1, 2, 3)), arbitrary, random);
	}

	@Property(tries = 10)
	void mapped(@ForAll Random random) {
		Arbitrary<String> arbitrary =
			Arbitraries.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map(String::valueOf);
		assertAllValuesAreShrunkTo("1", arbitrary, random);
	}

	@Property(tries = 10)
	void flatMapped(@ForAll Random random) {
		Arbitrary<Integer> arbitrary =
			Arbitraries.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
					   .flatMap(i -> Arbitraries.of(i));
		assertAllValuesAreShrunkTo(1, arbitrary, random);
	}

	@Property(tries = 10)
	void lazy(@ForAll Random random) {
		Arbitrary<Integer> arbitrary =
			Arbitraries.lazy(() -> Arbitraries.of(1, 2, 3, 4, 5, 6));
		assertAllValuesAreShrunkTo(1, arbitrary, random);
	}

	@Property(tries = 10)
	boolean forType(@ForAll Random random) {
		Arbitrary<Counter> arbitrary = Arbitraries.forType(Counter.class);
		Counter value = shrinkToEnd(arbitrary, random);

		// 0:1, 1:0, 0:-1 or -1:0
		return Math.abs(value.n1 + value.n2) == 1;
	}

	@Property(tries = 10)
	void collectedListShrinksElementsAndSize(@ForAll Random random) {
		Arbitrary<Integer> integersShrunkTowardMax =
			Arbitraries
				.integers()
				.between(1, 3)
				.map(i -> 4 - i);

		Arbitrary<List<Integer>> collected = integersShrunkTowardMax.collect(list -> sum(list) >= 12);
		RandomGenerator<List<Integer>> generator = collected.generator(10);

		Shrinkable<List<Integer>> shrinkable = generator.next(random);

		ShrinkingSequence<List<Integer>> sequence = shrinkable.shrink((TestingFalsifier<List<Integer>>) ignore1 -> false);
		sequence.init(FalsificationResult.falsified(shrinkable));

		while (sequence.next(() -> {}, ignore -> {})) ;
		assertThat(sequence.current().value()).containsExactly(3, 3, 3, 3);
	}

	private int sum(List<Integer> list) {
		return list.stream().mapToInt(i -> i).sum();
	}

	@Group
	class Maps {

		@Property(tries = 10)
		boolean mapIsShrunkToEmptyMap(@ForAll Random random) {
			Arbitrary<Integer> keys = Arbitraries.integers().between(-10, 10);
			Arbitrary<String> values = Arbitraries.strings().alpha().ofLength(1);

			SizableArbitrary<Map<Integer, String>> arbitrary = Arbitraries.maps(keys, values).ofMaxSize(10);

			return shrinkToEnd(arbitrary, random).isEmpty();
		}

		@Property(tries = 10)
		void mapIsShrunkToSmallestValue(@ForAll Random random) {
			Arbitrary<Integer> keys = Arbitraries.integers().between(-10, 10);
			Arbitrary<String> values = Arbitraries.strings().withCharRange('A', 'Z').ofLength(1);

			SizableArbitrary<Map<Integer, String>> arbitrary = Arbitraries.maps(keys, values).ofMaxSize(10);

			TestingFalsifier<Map<Integer, String>> sumOfKeysLessThan2 = map -> map.keySet().size() < 2;
			Map<Integer, String> map = falsifyThenShrink(arbitrary, random, sumOfKeysLessThan2);

			assertThat(map).hasSize(2);
			assertThat(map.keySet()).containsAnyOf(0, 1, -1);
			assertThat(map.values()).containsOnly("A");
		}

	}

	private static class Counter {
		public int n1, n2;

		public Counter(int n1, int n2) {
			if (n1 == n2) {
				throw new IllegalArgumentException("Numbers must not be equal");
			}
			this.n1 = n1;
			this.n2 = n2;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Counter counter = (Counter) o;

			if (n1 != counter.n1) return false;
			return n2 == counter.n2;
		}

		@Override
		public int hashCode() {
			int result = n1;
			result = 31 * result + n2;
			return result;
		}

		@Override
		public String toString() {
			return n1 + ":" + n2;
		}
	}
}
