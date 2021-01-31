package net.jqwik.api;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;

import net.jqwik.api.arbitraries.*;
import net.jqwik.api.constraints.*;
import net.jqwik.engine.*;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

import static net.jqwik.testing.ShrinkingSupport.*;
import static net.jqwik.testing.TestingSupport.*;

class ListArbitraryTests {

	@Example
	void list() {
		Arbitrary<String> stringArbitrary = Arbitraries.of("1", "hallo", "test");
		ListArbitrary<String> listArbitrary = stringArbitrary.list();

		RandomGenerator<List<String>> generator = listArbitrary.generator(1, true);
		assertGeneratedLists(generator, 0, Integer.MAX_VALUE);
	}

	@Example
	void ofSize() {
		Arbitrary<String> stringArbitrary = Arbitraries.of("1", "hallo", "test");
		ListArbitrary<String> listArbitrary = stringArbitrary.list().ofSize(42);

		RandomGenerator<List<String>> generator = listArbitrary.generator(1, true);
		assertGeneratedLists(generator, 42, 42);
	}

	@Example
	void ofMinSize_ofMaxSize() {
		Arbitrary<String> stringArbitrary = Arbitraries.of("1", "hallo", "test");
		ListArbitrary<String> listArbitrary = stringArbitrary.list().ofMinSize(2).ofMaxSize(5);

		RandomGenerator<List<String>> generator = listArbitrary.generator(1, true);
		assertGeneratedLists(generator, 2, 5);
	}

	@Example
	void reduceList(@ForAll Random random) {
		ListArbitrary<Integer> listArbitrary =
				Arbitraries.integers().between(1, 5).list().ofMinSize(1).ofMaxSize(10);

		Arbitrary<Integer> integerArbitrary = listArbitrary.reduce(0, Integer::sum);

		RandomGenerator<Integer> generator = integerArbitrary.generator(1000, true);

		assertAllGenerated(generator, random, sum -> {
			assertThat(sum).isBetween(1, 50);
		});

		assertAtLeastOneGenerated(generator, random, sum -> sum == 1);
		assertAtLeastOneGenerated(generator, random, sum -> sum > 30);
	}

	@Example
	void uniqueElements(@ForAll Random random) {
		ListArbitrary<Integer> listArbitrary =
				Arbitraries.integers().between(1, 1000).list().ofMaxSize(20)
						   .uniqueElements(i -> i % 100);

		RandomGenerator<List<Integer>> generator = listArbitrary.generator(1000, true);

		assertAllGenerated(generator, random, list -> {
			assertThat(isUniqueModulo(list, 100)).isTrue();
		});
	}

	@Property(tries = 10)
	void uniqueElementsWithoutMaxSize(@ForAll Random random, @ForAll @IntRange(max = 10) int minSize) {
		ListArbitrary<Integer> listArbitrary =
				Arbitraries.integers().between(1, 1000).list().ofMinSize(minSize)
						   .uniqueElements(i -> i % 10);

		RandomGenerator<List<Integer>> generator = listArbitrary.generator(1000, true);

		assertAllGenerated(generator, random, list -> {
			assertThat(isUniqueModulo(list, 10)).isTrue();
		});
	}

	@Example
	void uniqueElementsWithNull(@ForAll Random random) {
		ListArbitrary<Integer> listArbitrary =
				Arbitraries.integers().between(1, 1000).injectNull(0.5)
						   .list().ofMaxSize(20)
						   .uniqueElements(i -> i % 100);

		RandomGenerator<List<Integer>> generator = listArbitrary.generator(1000, true);

		assertAllGenerated(generator, random, list -> {
			assertThat(isUniqueModulo(list, 100)).isTrue();
		});
	}

	@Example
	void multipleUniquenessConstraints(@ForAll Random random) {
		ListArbitrary<Integer> listArbitrary =
				Arbitraries.integers().between(1, 1000).list().ofMaxSize(20)
						   .uniqueElements(i -> i % 99)
						   .uniqueElements(i -> i % 100);

		RandomGenerator<List<Integer>> generator = listArbitrary.generator(1000, true);

		assertAllGenerated(generator, random, list -> {
			assertThat(isUniqueModulo(list, 100)).isTrue();
			assertThat(isUniqueModulo(list, 99)).isTrue();
		});
	}

	@Example
	void uniquenessConstraintCannotBeFulfilled(@ForAll Random random) {
		ListArbitrary<Integer> listArbitrary =
				Arbitraries.integers().between(1, 1000).list().ofSize(10)
						   .uniqueElements(i -> i % 5);

		RandomGenerator<List<Integer>> generator = listArbitrary.generator(1000, true);

		Assertions.assertThrows(TooManyFilterMissesException.class, () -> generator.next(random));
	}

	@Example
	void uniquenessElements(@ForAll Random random) {
		ListArbitrary<Integer> listArbitrary =
				Arbitraries.integers().between(1, 1000).list().ofMaxSize(20).uniqueElements();

		RandomGenerator<List<Integer>> generator = listArbitrary.generator(1000, true);

		assertAllGenerated(generator, random, list -> {
			assertThat(isUniqueModulo(list, 1000)).isTrue();
		});
	}

	private boolean isUniqueModulo(List<Integer> list, int modulo) {
		List<Integer> moduloList = list.stream().map(i -> {
			if (i == null) {
				return null;
			}
			return i % modulo;
		}).collect(Collectors.toList());
		return new HashSet<>(moduloList).size() == list.size();
	}

	@Example
	void mapEach(@ForAll Random random) {
		Arbitrary<Integer> integerArbitrary = Arbitraries.integers().between(1, 10);
		Arbitrary<List<Tuple.Tuple2<Integer, List<Integer>>>> setArbitrary =
				integerArbitrary
						.list().ofSize(5)
						.mapEach((all, each) -> Tuple.of(each, all));

		RandomGenerator<List<Tuple.Tuple2<Integer, List<Integer>>>> generator = setArbitrary.generator(1, true);

		assertAllGenerated(generator, random, set -> {
			assertThat(set).hasSize(5);
			assertThat(set).allMatch(tuple -> tuple.get2().size() == 5);
		});
	}

	@Example
	void flatMapEach(@ForAll Random random) {
		Arbitrary<Integer> integerArbitrary = Arbitraries.integers().between(1, 10);
		Arbitrary<List<Tuple.Tuple2<Integer, Integer>>> setArbitrary =
				integerArbitrary
						.list().ofSize(5)
						.flatMapEach((all, each) ->
											 Arbitraries.of(all).map(friend -> Tuple.of(each, friend))
						);

		RandomGenerator<List<Tuple.Tuple2<Integer, Integer>>> generator = setArbitrary.generator(1, true);

		assertAllGenerated(generator, random, set -> {
			assertThat(set).hasSize(5);
			assertThat(set).allMatch(tuple -> tuple.get2() <= 10);
		});
	}

	@Group
	class ExhaustiveGeneration {

		@Example
		void listsAreCombinationsOfElementsUpToMaxLength() {
			Optional<ExhaustiveGenerator<List<Integer>>> optionalGenerator =
					Arbitraries.integers().between(1, 3).list().ofMaxSize(2).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<List<Integer>> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(13);
			assertThat(generator).containsExactly(
					asList(),
					asList(1),
					asList(2),
					asList(3),
					asList(1, 1),
					asList(1, 2),
					asList(1, 3),
					asList(2, 1),
					asList(2, 2),
					asList(2, 3),
					asList(3, 1),
					asList(3, 2),
					asList(3, 3)
			);
		}

		@Example
		void combinationsAreFilteredByUniquenessConstraints() {
			Optional<ExhaustiveGenerator<List<Integer>>> optionalGenerator =
					Arbitraries.integers().between(1, 3).list().ofMaxSize(2).uniqueElements(i -> i).exhaustive();
			assertThat(optionalGenerator).isPresent();

			ExhaustiveGenerator<List<Integer>> generator = optionalGenerator.get();
			assertThat(generator.maxCount()).isEqualTo(13);
			assertThat(generator).containsExactlyInAnyOrder(
					asList(),
					asList(1),
					asList(2),
					asList(3),
					asList(1, 2),
					asList(1, 3),
					asList(2, 1),
					asList(2, 3),
					asList(3, 1),
					asList(3, 2)
			);
		}

		@Example
		void elementArbitraryNotExhaustive() {
			Optional<ExhaustiveGenerator<List<Double>>> optionalGenerator =
					Arbitraries.doubles().between(1, 10).list().ofMaxSize(1).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}

		@Example
		void tooManyCombinations() {
			Optional<ExhaustiveGenerator<List<Integer>>> optionalGenerator =
					Arbitraries.integers().between(1, 10).list().ofMaxSize(10).exhaustive();
			assertThat(optionalGenerator).isNotPresent();
		}
	}

	@Group
	class EdgeCasesGeneration {

		@Example
		void edgeCases() {
			Arbitrary<Integer> ints = Arbitraries.of(-10, 10);
			Arbitrary<List<Integer>> arbitrary = ints.list();
			assertThat(collectEdgeCaseValues(arbitrary.edgeCases())).containsExactlyInAnyOrder(
					Collections.emptyList(),
					Collections.singletonList(-10),
					Collections.singletonList(10)
			);
			// make sure edge cases can be repeatedly generated
			assertThat(collectEdgeCaseValues(arbitrary.edgeCases())).hasSize(3);
		}

		@Example
		void edgeCasesWhenMinSize1() {
			IntegerArbitrary ints = Arbitraries.integers().between(-10, 10);
			Arbitrary<List<Integer>> arbitrary = ints.list().ofMinSize(1);
			assertThat(collectEdgeCaseValues(arbitrary.edgeCases())).containsExactlyInAnyOrder(
					Collections.singletonList(-10),
					Collections.singletonList(-9),
					Collections.singletonList(-2),
					Collections.singletonList(-1),
					Collections.singletonList(0),
					Collections.singletonList(1),
					Collections.singletonList(2),
					Collections.singletonList(9),
					Collections.singletonList(10)
			);
		}

		@Example
		void edgeCasesWhenMinSizeGreaterThan1() {
			IntegerArbitrary ints = Arbitraries.integers().between(-10, 10);
			Arbitrary<List<Integer>> arbitrary = ints.list().ofMinSize(2);
			assertThat(collectEdgeCaseValues(arbitrary.edgeCases())).isEmpty();
		}

		@Example
		void edgeCasesWhenFixedSize() {
			Arbitrary<Integer> ints = Arbitraries.of(10, 100);
			Arbitrary<List<Integer>> arbitrary = ints.list().ofSize(3);
			assertThat(collectEdgeCaseValues(arbitrary.edgeCases())).containsExactlyInAnyOrder(
					asList(10, 10, 10),
					asList(100, 100, 100)
			);
		}

		@Example
		void edgeCasesAreGeneratedFreshlyOnEachCallToIterator() {
			IntegerArbitrary ints = Arbitraries.integers().between(-1, 1);
			Arbitrary<List<Integer>> arbitrary = ints.list();
			EdgeCases<List<Integer>> edgeCases = arbitrary.edgeCases();

			for (Shrinkable<List<Integer>> listShrinkable : edgeCases) {
				listShrinkable.value().add(42);
			}

			Set<List<Integer>> values = collectEdgeCaseValues(edgeCases);
			assertThat(values).containsExactlyInAnyOrder(
					Collections.emptyList(),
					Collections.singletonList(-1),
					Collections.singletonList(0),
					Collections.singletonList(1)
			);
		}

		@Example
		void edgeCasesAreFilteredByUniquenessConstraints() {
			IntegerArbitrary ints = Arbitraries.integers().between(-10, 10);
			Arbitrary<List<Integer>> arbitrary = ints.list().ofSize(2).uniqueElements(i -> i);
			assertThat(collectEdgeCaseValues(arbitrary.edgeCases())).isEmpty();
		}

	}

	@Group
	@PropertyDefaults(tries = 100)
	class Shrinking {

		@Property
		void shrinksToEmptyListByDefault(@ForAll Random random) {
			ListArbitrary<Integer> lists = Arbitraries.integers().between(1, 10).list();
			List<Integer> value = falsifyThenShrink(lists, random);
			assertThat(value).isEmpty();
		}

		@Property
		void shrinkToMinSize(@ForAll Random random, @ForAll @IntRange(min = 1, max = 20) int min) {
			ListArbitrary<Integer> lists = Arbitraries.integers().between(1, 10).list().ofMinSize(min);
			List<Integer> value = falsifyThenShrink(lists, random);
			assertThat(value).hasSize(min);
			assertThat(value).containsOnly(1);
		}

		@Property
		void shrinkWithUniqueness(@ForAll Random random, @ForAll @IntRange(min = 2, max = 10) int min) {
			ListArbitrary<Integer> lists =
					Arbitraries.integers().between(1, 100).list().ofMinSize(min).ofMaxSize(10)
							   .uniqueElements(i -> i);
			List<Integer> value = falsifyThenShrink(lists, random);
			assertThat(value).hasSize(min);
			assertThat(isUniqueModulo(value, 100))
					.describedAs("%s is not unique mod 100", value)
					.isTrue();
			assertThat(value).allMatch(i -> i <= min);
		}

		@Property
		void shrinkWithUniquenessAndNulls(@ForAll Random random) {
			ListArbitrary<Integer> lists =
					Arbitraries.integers().between(1, 100).injectNull(0.5)
							   .list().ofMinSize(3).ofMaxSize(10)
							   .uniqueElements(i -> i);
			List<Integer> value = falsifyThenShrink(lists, random);
			assertThat(value).containsExactly(null, 1, 2);
		}

	}

	private void assertGeneratedLists(RandomGenerator<List<String>> generator, int minSize, int maxSize) {
		Random random = SourceOfRandomness.current();
		assertAllGenerated(generator, random, list -> {
			assertThat(list.size()).isBetween(minSize, maxSize);
			assertThat(list).isSubsetOf("1", "hallo", "test");
		});
	}

}
