package net.jqwik.engine.properties.shrinking;

import java.util.*;
import java.util.function.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.*;
import net.jqwik.engine.properties.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.api.ShrinkingTestHelper.*;
import static net.jqwik.api.Tuple.*;

@PropertyDefaults(tries = 100, afterFailure = AfterFailureMode.RANDOM_SEED)
class LazyOfArbitraryShrinkingTests {

	@Property
	void distance(@ForAll Random random) {
		Arbitrary<Integer> arbitrary =
			Arbitraries.lazyOf(
				() -> Arbitraries.integers().between(1, 10).filter(i -> i == 10)
			);

		Shrinkable<Integer> next = arbitrary.generator(10).next(random);
		assertThat(next.distance()).isEqualTo(ShrinkingDistance.of(0, 9));
	}

	@Property
	void shrinkToOtherSuppliers(@ForAll Random random) {
		Arbitrary<Integer> arbitrary =
			Arbitraries.lazyOf(
				() -> Arbitraries.integers().between(1, 10),
				() -> Arbitraries.integers().between(1, 20).filter(i -> i > 10),
				() -> Arbitraries.integers().between(1, 30).filter(i -> i > 20),
				() -> Arbitraries.integers().between(1, 40).filter(i -> i > 30)
			);
		Integer value = shrinkToMinimal(arbitrary, random);
		assertThat(value).isEqualTo(1);
	}

	@Property
	void twoLazyOfArbitraries(@ForAll Random random) {
		Arbitrary<Integer> arbitrary1 =
			Arbitraries.lazyOf(() -> Arbitraries.integers().between(1, 10));
		Arbitrary<Integer> arbitrary2 =
			Arbitraries.lazyOf(() -> Arbitraries.integers().between(11, 20));

		Arbitrary<Integer> arbitrary = Combinators.combine(arbitrary1, arbitrary2).as(Integer::sum);

		Integer value = shrinkToMinimal(arbitrary, random);
		assertThat(value).isEqualTo(12);
	}

	@Property
	void oneStep(@ForAll Random random) {
		Arbitrary<Integer> arbitrary =
			Arbitraries.lazyOf(Arbitraries::integers);
		Integer value = shrinkToMinimal(arbitrary, random);
		assertThat(value).isEqualTo(0);
	}

	@Property
	void severalStepsToList(@ForAll Random random) {
		Arbitrary<List<Integer>> arbitrary = listOfInteger();
		TestingFalsifier<List<Integer>> falsifier = integers -> integers.size() < 2;
		List<Integer> shrunkValue = falsifyThenShrink(arbitrary, random, falsifier);

		assertThat(shrunkValue).isEqualTo(Arrays.asList(1, 1));
	}

	@Provide
	Arbitrary<List<Integer>> listOfInteger() {
		return Arbitraries.lazyOf(
			() -> Arbitraries.integers().between(1, 5).list().ofSize(1),
			() -> Combinators.combine(listOfInteger(), listOfInteger()).as((l1, l2) -> {
				ArrayList<Integer> newList = new ArrayList<>(l1);
				newList.addAll(l2);
				return newList;
			})
		);
	}

	@Property(tries = 10)
		// Fewer tries to prevent occasional heap overflow in Travis build
	void severalStepsToList_withReversedOrderOfSuppliers(@ForAll Random random) {
		Arbitrary<List<Integer>> arbitrary = listOfIntegerReversedLazy();
		TestingFalsifier<List<Integer>> falsifier = integers -> integers.size() < 2;
		List<Integer> shrunkValue = falsifyThenShrink(arbitrary, random, falsifier);

		assertThat(shrunkValue).isEqualTo(Arrays.asList(1, 1));
	}

	@Provide
	Arbitrary<List<Integer>> listOfIntegerReversedLazy() {
		return Arbitraries.lazyOf(
			() -> Combinators.combine(listOfIntegerReversedLazy(), listOfIntegerReversedLazy()).as((l1, l2) -> {
				ArrayList<Integer> newList = new ArrayList<>(l1);
				newList.addAll(l2);
				return newList;
			}),
			() -> Arbitraries.integers().between(1, 5).list().ofSize(1)
		);
	}

	@Property
	void withDuplicateSuppliers(@ForAll Random random) {
		Arbitrary<List<Integer>> arbitrary = listOfIntegerWithDuplicateSuppliers();
		List<Integer> shrunkValue = falsifyThenShrink(arbitrary, random, alwaysFalsify());
		;
		assertThat(shrunkValue).isEqualTo(Collections.emptyList());
	}

	@Provide
	Arbitrary<List<Integer>> listOfIntegerWithDuplicateSuppliers() {
		return Arbitraries.lazyOf(
			() -> Arbitraries.just(new ArrayList<>()),
			() -> Arbitraries.just(new ArrayList<>()),
			() -> Arbitraries.integers().between(1, 5).list().ofSize(1),
			() -> Arbitraries.integers().between(1, 5).list().ofSize(1),
			() -> Combinators.combine(listOfIntegerWithDuplicateSuppliers(), listOfIntegerWithDuplicateSuppliers()).as((l1, l2) -> {
				ArrayList<Integer> newList = new ArrayList<>(l1);
				newList.addAll(l2);
				return newList;
			}),
			() -> Combinators.combine(listOfIntegerWithDuplicateSuppliers(), listOfIntegerWithDuplicateSuppliers()).as((l1, l2) -> {
				ArrayList<Integer> newList = new ArrayList<>(l1);
				newList.addAll(l2);
				return newList;
			})
		);
	}

	@Group
	class Calculator {

		@Property(tries = 1000)
		void depthIsBetweenZeroAndNumberOfNodes(@ForAll Random random) {
			Arbitrary<Object> arbitrary = expression();
			LazyOfShrinkable<Object> lazyOf = (LazyOfShrinkable<Object>) arbitrary.generator(10).next(random);

			int numberOfNodes = countNodes(lazyOf.value());
			assertThat(lazyOf.depth).isBetween(0, numberOfNodes);
		}

		/**
		 * Not all shrinking attempts reach the shortest possible expression of 5 nodes,
		 * a few (about 1% of cases) have larger results. That's why I fixed the seed to
		 * prevent flaky builds.
		 */
		@Property(seed = "17", tries = 1000)
		@ExpectFailure(checkResult = ShrinkToSmallExpression.class)
		//@Report(Reporting.FALSIFIED)
		void shrinkExpressionTree(@ForAll("expression") Object expression) {
			Assume.that(noDivByZero(expression));
			evaluate(expression);
		}

		private class ShrinkToSmallExpression implements Consumer<PropertyExecutionResult> {
			@Override
			public void accept(PropertyExecutionResult propertyExecutionResult) {
				List<Object> actual = propertyExecutionResult.falsifiedParameters().get();
				// The best shrinker should shrink to just 5 nodes:
				// ("/", 0, ("/", 0, 1)) or ("/", 0, ("+", 0, 0))
				assertThat(countNodes(actual.get(0))).isEqualTo(5);
			}
		}

		private int countNodes(Object expression) {
			if (expression instanceof Integer) {
				return 1;
			}
			;
			@SuppressWarnings("rawtypes")
			Tuple3 tupleExpression = (Tuple3) expression;
			return 1 + countNodes(tupleExpression.get2()) + countNodes(tupleExpression.get3());
		}

		private boolean noDivByZero(final Object expression) {
			if (expression instanceof Integer) {
				return true;
			}
			@SuppressWarnings("rawtypes")
			Tuple3 tupleExpression = (Tuple3) expression;
			if (tupleExpression.get1().equals("/") && tupleExpression.get3().equals(0)) {
				return false;
			}
			return noDivByZero(tupleExpression.get2()) && noDivByZero(tupleExpression.get3());
		}

		@Provide
		Arbitrary<Object> expression() {
			return Arbitraries.lazyOf(
				Arbitraries::integers,
				Arbitraries::integers,
				Arbitraries::integers,
				() -> Combinators.combine(expression(), expression())
								 .as((e1, e2) -> of("+", e1, e2)),
				() -> Combinators.combine(expression(), expression())
								 .as((e1, e2) -> of("/", e1, e2))
			);
		}

		int evaluate(Object expression) {
			if (expression instanceof Integer) {
				return (int) expression;
			}
			@SuppressWarnings("rawtypes")
			Tuple3 tupleExpression = (Tuple3) expression;
			if (tupleExpression.get1().equals("+")) {
				return evaluate(tupleExpression.get2()) + evaluate(tupleExpression.get3());
			}
			if (tupleExpression.get1().equals("/")) {
				return evaluate(tupleExpression.get2()) / evaluate(tupleExpression.get3());
			}
			throw new IllegalArgumentException(String.format("%s is not a valid expression", expression));
		}

	}

}
