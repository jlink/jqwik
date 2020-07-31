package net.jqwik.api;

import java.util.*;
import java.util.function.*;

import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.api.NEW_ShrinkingTestHelper.*;

class FunctionsTests {

	@Property(tries = 100)
	void manyCallsToFunction(@ForAll("stringToIntegerFunctions") Function<String, Integer> function, @ForAll @AlphaChars String aString) {
		Integer valueForHello = function.apply(aString);
		assertThat(valueForHello).isBetween(-100, 100);
		assertThat(function.apply(aString)).isEqualTo(valueForHello);

		int valueForHelloPlus1 = function.andThen(i -> i + 1).apply(aString);
		assertThat(valueForHelloPlus1).isEqualTo(valueForHello + 1);
	}

	@Provide
	Arbitrary<Function<String, Integer>> stringToIntegerFunctions() {
		Arbitrary<Integer> integers = Arbitraries.integers().between(-100, 100);
		return Functions.function(Function.class).returns(integers);
	}

	@Example
	void function_creates_same_result_for_same_input(@ForAll Random random) {
		Arbitrary<Integer> integers = Arbitraries.integers().between(1, 10);
		Arbitrary<Function<String, Integer>> functions =
			Functions.function(Function.class).returns(integers);

		Function<String, Integer> function = functions.generator(10).next(random).value();

		Integer valueForHello = function.apply("hello");
		assertThat(valueForHello).isBetween(1, 10);
		assertThat(function.apply("hello")).isEqualTo(valueForHello);
	}

	@Example
	void some_functions_create_different_result_for_different_input() {
		Arbitrary<Integer> integers = Arbitraries.integers().between(1, 10);
		Arbitrary<Function<String, Integer>> functions =
			Functions.function(Function.class).returns(integers);

		ArbitraryTestHelper.assertAtLeastOneGenerated(
			functions.generator(10),
			function -> !function.apply("value1").equals(function.apply("value2"))
		);
	}

	@Example
	void toString_of_functions_can_be_called(@ForAll Random random) {
		Arbitrary<Integer> integers = Arbitraries.just(42);
		Arbitrary<Function<String, Integer>> functions =
			Functions.function(Function.class).returns(integers);

		Function<String, Integer> function = functions.generator(10).next(random).value();
		assertThat(function.toString()).contains("Function");
	}

	@Example
	void hashCode_of_functions_can_be_called(@ForAll Random random) {
		Arbitrary<Integer> integers = Arbitraries.integers().between(1, 10000);
		Arbitrary<Function<String, Integer>> functions =
			Functions.function(Function.class).returns(integers);

		RandomGenerator<Function<String, Integer>> generator = functions.generator(10);
		Function<String, Integer> function1 = generator.next(random).value();

		assertThat(function1.hashCode()).isEqualTo(function1.hashCode());
		Function<String, Integer> function2 = generator.next(random).value();
		while (function2.apply("a") == function1.apply("a")) {
			function2 = generator.next(random).value();
		}
		assertThat(function1.hashCode()).isNotEqualTo(function2.hashCode());
	}

	@Example
	void equals_of_functions_can_be_called(@ForAll Random random) {
		Arbitrary<Integer> integers = Arbitraries.integers().between(1, 10000);
		Arbitrary<Function<String, Integer>> functions =
			Functions.function(Function.class).returns(integers);

		RandomGenerator<Function<String, Integer>> generator = functions.generator(10);
		Function<String, Integer> function1 = generator.next(random).value();

		assertThat(function1.equals(function1)).isTrue();
		assertThat(function1.equals(generator.next(random).value())).isFalse();
	}

	@Example
	void default_methods_of_functions_can_be_called(@ForAll Random random) {
		Arbitrary<Integer> integers = Arbitraries.just(42);
		Arbitrary<Function<String, Integer>> functions =
			Functions.function(Function.class).returns(integers);

		Function<String, Integer> function = functions.generator(10).next(random).value();

		Function<String, Integer> andThenFunction = function.andThen(value -> value - 1);
		assertThat(function.apply("any")).isEqualTo(42);
		assertThat(andThenFunction.apply("any")).isEqualTo(41);
	}

	@Example
	void default_methods_of_self_made_functional_interface_can_be_called(@ForAll Random random) {
		Arbitrary<Integer> integers = Arbitraries.just(42);
		Arbitrary<MyFunctionalInterface<String, String, Integer>> functions =
			Functions.function(MyFunctionalInterface.class).returns(integers);

		MyFunctionalInterface<String, String, Integer> function = functions.generator(10).next(random).value();
		assertThat(function.hello()).isEqualTo("hello");
	}

	@Example
	void null_value_is_accepted_as_input() {
		Arbitrary<Integer> integers = Arbitraries.integers().between(1, 10);
		Arbitrary<Function<String, Integer>> functions =
			Functions.function(Function.class).returns(integers);

		ArbitraryTestHelper.assertAllGenerated(
			functions.generator(10),
			function -> function.apply(null) != null
		);
	}

	@Example
	void supplier_always_returns_same_element(@ForAll Random random) {
		Arbitrary<Integer> integers = Arbitraries.integers().between(1, 10);
		Arbitrary<Supplier<Integer>> functions =
			Functions.function(Supplier.class).returns(integers);

		Supplier<Integer> supplier = functions.generator(10).next(random).value();

		Integer value = supplier.get();
		assertThat(value).isBetween(1, 10);
		assertThat(supplier.get()).isEqualTo(value);
	}

	@Example
	void consumer_accepts_anything(@ForAll Random random) {
		Arbitrary<Consumer<Integer>> functions =
			Functions.function(Consumer.class).returns(Arbitraries.nothing());

		Consumer<Integer> supplier = functions.generator(10).next(random).value();

		supplier.accept(0);
		supplier.accept(Integer.MAX_VALUE);
	}

	@Example
	void functional_interfaces_and_SAM_types_are_accepted() {
		Arbitrary<Integer> any = Arbitraries.just(1);

		assertThat(Functions.function(Function.class).returns(any)).isNotNull();
		assertThat(Functions.function(Supplier.class).returns(any)).isNotNull();
		assertThat(Functions.function(Consumer.class).returns(Arbitraries.nothing())).isNotNull();
		assertThat(Functions.function(Predicate.class).returns(any)).isNotNull();
		assertThat(Functions.function(MyFunctionalInterface.class).returns(any)).isNotNull();
		assertThat(Functions.function(MyInheritedFunctionalInterface.class).returns(any)).isNotNull();
		assertThat(Functions.function(MySamType.class).returns(any)).isNotNull();
		assertThat(Functions.function(IntTransformer.class).returns(any)).isNotNull();
	}

	@Example
	void non_functional_interfaces_are_not_accepted() {
		Arbitrary<Integer> any = Arbitraries.just(1);

		assertThatThrownBy(
			() -> Functions.function(NotAFunctionalInterface.class).returns(any))
			.isInstanceOf(JqwikException.class);
		assertThatThrownBy(
			() -> Functions.function(MyAbstractClass.class).returns(any))
			.isInstanceOf(JqwikException.class);
	}

	@Property(tries = 100, afterFailure = AfterFailureMode.RANDOM_SEED)
	void functions_are_shrunk_to_constant_functions(@ForAll Random random) {
		Arbitrary<Integer> integers = Arbitraries.integers().between(1, 20);
		Arbitrary<Function<String, Integer>> functions =
			Functions.function(Function.class).returns(integers);

		Falsifier<Function<String, Integer>> falsifier =
			f -> (f.apply("value1") < 11) ?
					 TryExecutionResult.satisfied() :
					 TryExecutionResult.falsified(null);

		Function<String, Integer> shrunkFunction = falsifyThenShrink(functions, random, falsifier);
		assertThat(shrunkFunction.apply("value1")).isEqualTo(11);

		assertThat(shrunkFunction.apply("value2")).isEqualTo(11);
		assertThat(shrunkFunction.apply("any")).isEqualTo(11);
	}

	@Property(tries = 100)
	@ExpectFailure(checkResult = ShrinkToConstantFunction.class)
	void functions_are_shrunk_to_constant_functions(
		@ForAll Function<String, Integer> function,
		@ForAll @AlphaChars @StringLength(1) String aString
	) {
		int result = function.apply(aString);
		assertThat(result).isLessThan(11);
	}

	@SuppressWarnings("unchecked")
	private class ShrinkToConstantFunction implements Consumer<PropertyExecutionResult> {
		@Override
		public void accept(PropertyExecutionResult result) {
			Function<String, Integer> function = (Function<String, Integer>) result.falsifiedParameters().get().get(0);
			String string = (String) result.falsifiedParameters().get().get(1);
			assertThat(function.apply(string)).isEqualTo(11);
		}
	}

	@Group
	class Conditional_results {
		@Example
		void function_with_conditional_answer() {
			Arbitrary<Integer> integers = Arbitraries.integers().between(1, 100);
			Arbitrary<Function<String, Integer>> functions =
				Functions
					.function(Function.class).returns(integers)
					.when(params -> params.get(0).equals("three"), params -> 3)
					.when(params -> params.get(0).equals("four"), params -> 4);

			ArbitraryTestHelper.assertAllGenerated(
				functions.generator(10),
				function -> function.apply("three") == 3 && function.apply("four") == 4
			);
		}

		@Example
		void first_matching_conditional_answer_is_used() {
			Arbitrary<Integer> integers = Arbitraries.integers().between(1, 100);
			Arbitrary<Function<String, Integer>> functions =
				Functions
					.function(Function.class).returns(integers)
					.when(params -> params.get(0).equals("three"), params -> 3)
					.when(params -> params.get(0).equals("three"), params -> 33);

			ArbitraryTestHelper.assertAllGenerated(
				functions.generator(10),
				function -> function.apply("three") == 3
			);
		}

		@Example
		void function_with_conditional_null_answer() {
			Arbitrary<String> integers = Arbitraries.of("1", "2", "3");
			Arbitrary<Function<String, String>> functions =
				Functions
					.function(Function.class).returns(integers)
					.when(params -> params.get(0).equals("null"), params -> null);

			ArbitraryTestHelper.assertAllGenerated(
				functions.generator(10),
				function -> function.apply("null") == null
			);
		}

		@Example
		void function_with_conditional_exception() {
			Arbitrary<Integer> integers = Arbitraries.integers().between(1, 100);
			Arbitrary<Function<String, Integer>> functions =
				Functions
					.function(Function.class).returns(integers)
					.when(params -> params.get(0) == null, params -> {
						throw new IllegalArgumentException();
					});

			ArbitraryTestHelper.assertAllGenerated(
				functions.generator(10),
				function -> {
					assertThatThrownBy(
						() -> function.apply(null)
					).isInstanceOf(IllegalArgumentException.class);
				}
			);
		}

		@Example
		void conditional_answer_works_when_shrunk(@ForAll Random random) {
			Arbitrary<Integer> integers = Arbitraries.integers().between(1, 100);
			Arbitrary<Function<String, Integer>> functions =
				Functions
					.function(Function.class).returns(integers)
					.when(params -> params.get(0).equals("three"), params -> 3);

			Function<String, Integer> shrunkFunction = ShrinkingTestHelper.shrinkToEnd(functions, random);

			assertThat(shrunkFunction.apply("three")).isEqualTo(3);
		}

	}

	interface MySamType<P1, P2, R> {
		R take(P1 p1, P2 p2);
	}

	interface MySupplier<R> {
		R take();
	}

	interface MyConsumer<P> {
		void take(P p);
	}

	@FunctionalInterface
	public interface MyFunctionalInterface<P1, P2, R> {
		R take(P1 p1, P2 p2);

		// Default method invocation only works for public interfaces
		default String hello() {
			return "hello";
		}
	}

	interface MyInheritedFunctionalInterface<P1, P2, R> extends MyFunctionalInterface {
	}

	interface IntTransformer {
		int transform(int anInt);
	}

	interface NotAFunctionalInterface<P1, P2, R> {
		R take1(P1 p1, P2 p2);

		R take2(P1 p1, P2 p2);
	}

	static abstract class MyAbstractClass<P1, P2, R> {
		abstract R take(P1 p1, P2 p2);
	}

}
