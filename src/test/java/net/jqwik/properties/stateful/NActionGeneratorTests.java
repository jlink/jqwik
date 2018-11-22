package net.jqwik.properties.stateful;

import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.stateful.*;

import static org.assertj.core.api.Assertions.*;

@Group
class NActionGeneratorTests {

	private static final Action<Integer> PLUS_2 = new Action<Integer>() {
		@Override
		public Integer run(Integer anInt) {
			return anInt + 2;
		}

		@Override
		public String toString() {
			return "+2";
		}
	};

	private static final Action<Integer> PLUS_1 = new Action<Integer>() {
		@Override
		public Integer run(Integer anInt) {
			return anInt + 2;
		}

		@Override
		public String toString() {
			return "+2";
		}
	};

	@Group
	class RandomGenerator {

		@Example
		void generatesActionsFromArbitrary(@ForAll Random random) {
			Arbitrary<Action<Integer>> samples = Arbitraries.samples(plus1(), plus2());

			NRandomActionGenerator<Integer> actionGenerator = new NRandomActionGenerator<>(samples, 1000, random);

			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);

			assertThat(actionGenerator.generated()).hasSize(6);
		}

		@Example
		void ignoresActionsWithFailingPrecondition(@ForAll Random random) {
			Arbitrary<Action<Integer>> samples = Arbitraries.samples(plus1(), plus2(), failedPrecondition());

			NRandomActionGenerator<Integer> actionGenerator = new NRandomActionGenerator<>(samples, 1000, random);

			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);

			assertThat(actionGenerator.generated()).hasSize(4);
		}
	}

	@Group
	class FromShrinkables {
		@Example
		void generateActionsFromListOfShrinkables() {
			List<Shrinkable<Action<Integer>>> shrinkables = Arrays.asList(
				Shrinkable.unshrinkable(PLUS_1),
				Shrinkable.unshrinkable(PLUS_2),
				Shrinkable.unshrinkable(PLUS_1)
			);

			NShrinkablesActionGenerator<Integer> actionGenerator = new NShrinkablesActionGenerator<>(shrinkables);

			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.generated()).hasSize(3);

			assertThatThrownBy(() -> actionGenerator.next(42)).isInstanceOf(NoSuchElementException.class);
		}

		@Example
		void filterOutFailingPreconditions() {
			List<Shrinkable<Action<Integer>>> shrinkables = Arrays.asList(
				Shrinkable.unshrinkable(PLUS_1),
				Shrinkable.unshrinkable(PLUS_2),
				Shrinkable.unshrinkable(failedPrecondition()),
				Shrinkable.unshrinkable(PLUS_1)
			);

			NShrinkablesActionGenerator<Integer> actionGenerator = new NShrinkablesActionGenerator<>(shrinkables);

			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.generated()).hasSize(3);
		}
	}

	@Group
	class FromListOfActions {
		@Example
		void generateActionsFromListOfShrinkables() {
			List<Action<Integer>> listOfActions = Arrays.asList(
				PLUS_1,
				PLUS_2,
				PLUS_1
			);

			NListActionGenerator<Integer> actionGenerator = new NListActionGenerator<>(listOfActions);

			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.generated()).hasSize(3);

			assertThatThrownBy(() -> actionGenerator.next(42)).isInstanceOf(NoSuchElementException.class);
		}

		@Example
		void filterOutFailingPreconditions() {
			List<Action<Integer>> listOfActions = Arrays.asList(
				PLUS_1,
				PLUS_2,
				failedPrecondition(),
				PLUS_1
			);

			NListActionGenerator<Integer> actionGenerator = new NListActionGenerator<>(listOfActions);

			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_2);
			assertThat(actionGenerator.next(42)).isEqualTo(PLUS_1);
			assertThat(actionGenerator.generated()).hasSize(3);
		}

	}

	private Action<Integer> plus1() {
		return PLUS_1;
	}

	private Action<Integer> plus2() {
		return PLUS_2;
	}

	private Action<Integer> failedPrecondition() {
		return new Action<Integer>() {
			@Override
			public boolean precondition(Integer model) {
				return false;
			}

			@Override
			public Integer run(Integer anInt) {
				return anInt + 100;
			}

			@Override
			public String toString() {
				return "failedPrecondition";
			}
		};
	}

}
